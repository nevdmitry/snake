package ru.codebattle.client;

import java.io.*;
import java.net.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.stream.*;

import ru.codebattle.client.api.*;
import ru.codebattle.client.api.GameBoard.*;

public class Main {

  private static final String HOST = "http://codebattle-pro-2020s1.westeurope.cloudapp.azure"
                                     + ".com/codenjoy-contest/board/player/u3qcl9tx74wl200n9aua?code=4228936592710077119&gameName=snakebattle";

  private static volatile BoardPoint mySnakeHead;

  private static volatile boolean isEvil;
  private static volatile GameBoard gameBoard;
  private static volatile SnakeAction currMove;

  public static void main(String[] args) throws URISyntaxException, IOException {
    SnakeBattleClient client = new RSnakeBattleClient();
    connect(client);

    System.in.read();

    client.initiateExit();
  }

  private static void connect(SnakeBattleClient client) {
    client.run(theBoard -> {
      try {
        BoardPoint head = theBoard.getMyHead();
        if (head == null || theBoard.getElementAt(head) == BoardElement.HEAD_SLEEP
            || theBoard.getElementAt(head) == BoardElement.HEAD_DEAD) {
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
      return safeAction(theBoard);
    });
  }

  static final String DELIM = "------------------------------------------------------------------------------------";

  public static class Rerun {

    public static void main(String[] args) throws Exception {
      StringBuilder board = new StringBuilder();

      try (BufferedReader reader = new BufferedReader(new FileReader(new File("20200528_022326.txt")))) {
        String line = "";
        while (null != (line = reader.readLine())) {
          if (line.equals(Main.DELIM)) {
            if (board.length() > 0) {
              GameBoard theBoard = new GameBoard(board.toString());
              theBoard.printBoard();
              safeAction(theBoard);
              board.setLength(0);
            }
            System.in.read();
          } else {
            board.append(line);
          }
        }
      }
    }
  }

  private static final AtomicReference<PrintWriter> gameLog = new AtomicReference<>();

  private static SnakeAction safeAction(GameBoard theBoard) {
    try {
      SnakeAction action = action(theBoard);
      gameBoard.planTo(action);
      return action;
    } catch (RuntimeException e) {
      e.printStackTrace();
      return currMove != null ? currMove : new SnakeAction(false, null);
    }
  }

  private static SnakeAction action(GameBoard theBoard) {
    gameBoard = gameBoard == null ? theBoard : gameBoard.next(theBoard);

    BoardPoint head = gameBoard.getMyHead();
    if (head == null) {
      mySnakeHead = null;
      return currMove == null ? new SnakeAction(Direction.RIGHT) : currMove;
    }
    BoardElement elementAt = gameBoard.getElementAt(head);
    if (elementAt == BoardElement.HEAD_DEAD) {
      mySnakeHead = null;
      return currMove;
    } else if (elementAt == BoardElement.HEAD_SLEEP) {
      mySnakeHead = null;
      return currMove == null ? new SnakeAction(Direction.RIGHT) : currMove;
    }

    if (mySnakeHead == null) {
      mySnakeHead = head;
    } else {
      if (!head.equals(mySnakeHead)) {
        mySnakeHead = head;
      }
    }

    isEvil = gameBoard.getEvilRemains() > 0;

    theBoard.recalculate(mySnakeHead);

    Direction futureDirection = null;
    if (gameBoard.isLastSnake()) {
      futureDirection = theBoard.getGoodThings().stream()
          .filter(elemStat -> {
            if (elemStat.getEvilSnake() != null
                || elemStat.getNonEvilSnake() != null) {
              return true;
            }
            return false;
          }).findFirst()
          .map(GameBoard.ElemStat::getDirection)
          .orElse(null);
    }
    if (futureDirection == null) {
      futureDirection = theBoard.getGoodThings().stream()
          .filter(elemStat -> {
            Direction direction = elemStat.getDirection();
            BoardPoint point = elemStat.getPoint();
            if (elemStat.getEvilSnake() != null
                || elemStat.getNonEvilSnake() != null) {
              return true;
            }

            List<BoardPoint> near = Stream.of(Direction.values())
                .filter(d -> d != direction.opposite())
                .map(d -> d.move(point))
                .collect(Collectors.toList());

            if (isEvil) {
              return gameBoard.getEvilSnakes().stream()
                  .filter(s -> s.getLength() > gameBoard.getMySnakeLength() - 2)
                  .noneMatch(s -> near.stream().anyMatch(p -> s.getBodyPoints().contains(p)));
            }
            Stream<Snake> evilDanger = gameBoard.getEvilSnakes().stream();
            Stream<Snake> nonEvil = gameBoard.getNotEvilSnakes().stream()
                .filter(s -> s.getLength() > gameBoard.getMySnakeLength() - 2);

            return Stream.concat(evilDanger, nonEvil).
                noneMatch(s -> near.stream().anyMatch(p -> s.getBodyPoints().contains(p)))
                   && gameBoard.getNotEvilSnakes().stream()
                       .map(s -> s.getBodyPoints().get(0))
                       .noneMatch(p -> gameBoard.distance(p, point) < gameBoard.getMySnakeLength());

          })
          .findFirst()
          .map(GameBoard.ElemStat::getDirection)
          .orElse(null);
    }

    if (!theBoard.getGoodThings().isEmpty()) {
      GameBoard.ElemStat stat = theBoard.getGoodThings().iterator().next();
    }
    if (futureDirection == null) {
      futureDirection = getDirectionsByDistance().stream()
          .map(direction -> moveScore(direction, head))
          .filter(d -> d.getValue() >= 0)
          .map(Map.Entry::getKey)
          .findFirst().orElse(null);
    }

    if (futureDirection == null) {
      futureDirection = getRandomDirections().stream()
          .map(direction -> moveScore(direction, head))
          .max(Comparator.comparingInt(Map.Entry::getValue))
          .map(Map.Entry::getKey)
          .orElse(null);

      if (futureDirection == null) {
        getRandomDirections().stream()
            .map(d -> new AbstractMap.SimpleEntry<>(d, profit(d.move(head))))
            .max(Comparator.comparingInt(AbstractMap.SimpleEntry::getValue))
            .map(Map.Entry::getKey)
            .orElseGet(() -> {
              Direction direction = getRandomDirections().get(0);
              return direction;
            });
      }
    }
    return createAction(futureDirection);
  }

  private static SnakeAction createAction(Direction futureDirection) {
    if (!tooYoungToDie()) {
      return SnakeAction.suicide();
    }

    mySnakeHead = futureDirection.move(gameBoard.getMyHead());
    if (shouldPreAct(futureDirection)) {
      currMove = SnakeAction.preAct(futureDirection);
    } else if (shouldPostAct(futureDirection)) {
      currMove = SnakeAction.postAct(futureDirection);
    } else {
      currMove = new SnakeAction(futureDirection);
    }

    return currMove;
  }

  private static boolean tooYoungToDie() {
    return gameBoard.getMySnakeLength() + 3 > gameBoard.getAverageEnemyLength()
           || gameBoard.getMySnakeLength() + 5 > gameBoard.getMaxEnemyLength();
  }

  private static boolean shouldPreAct(Direction futureDirection) {
    if (gameBoard.getStonesRemains() > 0) {
      if (gameBoard.getEvilRemains() > 1) {
        return true;
      }
    }
    return false;
  }

  private static boolean shouldPostAct(Direction futureDirection) {
    if (gameBoard.getStonesRemains() > 0) {
      if (gameBoard.getEvilRemains() > 0) {
        return true;
      }
    }
    return false;
  }

  private static List<Direction> getDirectionsByDistance() {
    SortedMap<Long, List<BoardPoint>> byDistanceMap = gameBoard.getByDistanceMap();
    if (isEvil) {
      SortedMap<Long, List<BoardPoint>> all = new TreeMap<>(byDistanceMap);
      gameBoard.getByDistanceEnemyTargets().forEach((key, value) -> all.merge(key, value,
                                                                              (bp1, bp2) -> {
                                                                                bp1.addAll(bp2);
                                                                                return bp1;
                                                                              }));
      gameBoard.getByDistanceStones().forEach((key, value) -> all.merge(key, value,
                                                                        (bp1, bp2) -> {
                                                                          bp1.addAll(bp2);
                                                                          return bp1;
                                                                        }));
      return getBySimplePrioritizing(all);
    }

    return getBySimplePrioritizing(byDistanceMap);
  }

  private static List<Direction> getBySimplePrioritizing(SortedMap<Long, List<BoardPoint>> byDistanceMap) {
    Map<Direction, Double> map = new EnumMap<>(Direction.class);
    BoardPoint head = mySnakeHead;

    byDistanceMap.entrySet()
        .stream()
        .limit(1)
        .forEach(dist2bpList -> dist2bpList.getValue()
            .forEach(bp -> {
              double weight = 1d / dist2bpList.getKey();
              if (bp.getY() > head.getY()) {
                map.merge(Direction.DOWN, weight, (key, oldValue) -> oldValue + weight);
              }
              if (bp.getY() < head.getY()) {

                map.merge(Direction.UP, weight, (key, oldValue) -> oldValue + weight);
              }
              if (bp.getX() > head.getX()) {
                map.merge(Direction.RIGHT, weight, (key, oldValue) -> oldValue + weight);
              }
              if (bp.getX() < head.getX()) {
                map.merge(Direction.LEFT, weight, (key, oldValue) -> oldValue + weight);
              }
            }));
    List<Direction> directions = map.entrySet().stream()
        .sorted(Comparator.<Map.Entry<Direction, Double>>comparingDouble(Map.Entry::getValue).reversed())
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());
    return directions;
  }

  private static List<Direction> getRandomDirections() {
    List<Direction> directionList = new ArrayList<>(Arrays.asList(Direction.values()));
    Collections.shuffle(directionList);
    return directionList;
  }

  private static Map.Entry<Direction, Integer> moveScore(Direction direction, BoardPoint head) {
    BoardPoint futureHead = direction.move(head);
    if (isDeadEnd(futureHead, head)) {
      return new AbstractMap.SimpleEntry<>(direction, -1);
    }
    return new AbstractMap.SimpleEntry<>(direction, profit(futureHead));
  }

  private static boolean isDeadEnd(BoardPoint futureHead, BoardPoint currHead) {
    if (profit(futureHead) < 0) {
      return false;
    }
    boolean b = Arrays.stream(Direction.values())
        .map(d -> d.move(futureHead))
        .filter(stepFromFuturePoint -> !stepFromFuturePoint.equals(currHead))
        .map(Main::profit)
        .allMatch(p -> p < 0);
    return b;
  }

  private static int profit(BoardPoint point) {
    if (point.isOutOfBoard(gameBoard.getSize())) {
      return -1;
    }
    BoardElement futureElement = gameBoard.getElementAt(point);
    switch (futureElement) {
      case GOLD:
        return 3;
      case APPLE:
        return 2;
      case FURY_PILL:
        return 4;
      case NONE:
        return 1;
      case STONE:
        if (isEvil) {
          return 3;
        }
        return gameBoard.getMySnakeLength() > 10 ? 0 : -1;
      default: {
        if (isEvil && gameBoard.isNotEvilEnemyHead(futureElement)) {
          return 10;
        }
        if (isEvil && gameBoard.isEnemyBody(futureElement)) {
          return 5;
        }
        return -1;
      }
    }
  }

  private static class RSnakeBattleClient extends SnakeBattleClient {

    public RSnakeBattleClient() throws URISyntaxException {
      super(Objects.requireNonNull(HOST));
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
      super.onClose(code, reason, remote);
      boolean interrupted = Thread.interrupted();
      try {
        Thread.currentThread().join(500);
        System.err.println("reconnect");
        Main.connect(new RSnakeBattleClient());
      } catch (URISyntaxException ignore) {
      } catch (InterruptedException e) {
        interrupted = true;
      } finally {
        if (interrupted) {
          Thread.currentThread().interrupt();
        }
      }
    }
  }
}
