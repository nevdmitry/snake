package ru.codebattle.client.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.val;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;

import static ru.codebattle.client.api.BoardElement.APPLE;
import static ru.codebattle.client.api.BoardElement.BODY_HORIZONTAL;
import static ru.codebattle.client.api.BoardElement.BODY_LEFT_DOWN;
import static ru.codebattle.client.api.BoardElement.BODY_LEFT_UP;
import static ru.codebattle.client.api.BoardElement.BODY_RIGHT_DOWN;
import static ru.codebattle.client.api.BoardElement.BODY_RIGHT_UP;
import static ru.codebattle.client.api.BoardElement.BODY_VERTICAL;
import static ru.codebattle.client.api.BoardElement.ENEMY_BODY_HORIZONTAL;
import static ru.codebattle.client.api.BoardElement.ENEMY_BODY_LEFT_DOWN;
import static ru.codebattle.client.api.BoardElement.ENEMY_BODY_LEFT_UP;
import static ru.codebattle.client.api.BoardElement.ENEMY_BODY_RIGHT_DOWN;
import static ru.codebattle.client.api.BoardElement.ENEMY_BODY_RIGHT_UP;
import static ru.codebattle.client.api.BoardElement.ENEMY_BODY_VERTICAL;
import static ru.codebattle.client.api.BoardElement.ENEMY_HEAD_DOWN;
import static ru.codebattle.client.api.BoardElement.ENEMY_HEAD_EVIL;
import static ru.codebattle.client.api.BoardElement.ENEMY_HEAD_LEFT;
import static ru.codebattle.client.api.BoardElement.ENEMY_HEAD_RIGHT;
import static ru.codebattle.client.api.BoardElement.ENEMY_HEAD_SLEEP;
import static ru.codebattle.client.api.BoardElement.ENEMY_HEAD_UP;
import static ru.codebattle.client.api.BoardElement.ENEMY_TAIL_END_DOWN;
import static ru.codebattle.client.api.BoardElement.ENEMY_TAIL_END_LEFT;
import static ru.codebattle.client.api.BoardElement.ENEMY_TAIL_END_RIGHT;
import static ru.codebattle.client.api.BoardElement.ENEMY_TAIL_END_UP;
import static ru.codebattle.client.api.BoardElement.ENEMY_TAIL_INACTIVE;
import static ru.codebattle.client.api.BoardElement.FLYING_PILL;
import static ru.codebattle.client.api.BoardElement.FURY_PILL;
import static ru.codebattle.client.api.BoardElement.GOLD;
import static ru.codebattle.client.api.BoardElement.HEAD_DEAD;
import static ru.codebattle.client.api.BoardElement.HEAD_DOWN;
import static ru.codebattle.client.api.BoardElement.HEAD_EVIL;
import static ru.codebattle.client.api.BoardElement.HEAD_FLY;
import static ru.codebattle.client.api.BoardElement.HEAD_LEFT;
import static ru.codebattle.client.api.BoardElement.HEAD_RIGHT;
import static ru.codebattle.client.api.BoardElement.HEAD_SLEEP;
import static ru.codebattle.client.api.BoardElement.HEAD_UP;
import static ru.codebattle.client.api.BoardElement.NONE;
import static ru.codebattle.client.api.BoardElement.START_FLOOR;
import static ru.codebattle.client.api.BoardElement.STONE;
import static ru.codebattle.client.api.BoardElement.TAIL_INACTIVE;
import static ru.codebattle.client.api.BoardElement.WALL;

public class GameBoard {

    @Getter
    private final String prettyBoardString;
    private BoardPoint futureHead;
    private BoardElement futureElement;

    public GameBoard(String boardString) {
        this.boardString = boardString.replace("\n", "");
        this.size = (int) Math.sqrt(boardString.length());
        String prettyBoard = "";
        for (int i = 0; i < size; i++) {
            prettyBoard+=this.boardString.substring(i * size, size * (i + 1))+"\n";
        }
        this.prettyBoardString = prettyBoard;
        this.board = new BoardElement[size][size];
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                this.board[x][y] = BoardElement.valueOf(boardString.charAt(y * size + x));
            }
        }
    }

    public GameBoard next(GameBoard board) {
        BoardPoint head = board.getMyHead();
        if (futureHead != null && futureHead.equals(head)) {
            if (futureElement == STONE) {
                board.stonesRemains = stonesRemains + 1;
            } else {
                board.stonesRemains = stonesRemains;
            }
            if (futureElement == FURY_PILL) {
                board.evilRemains += 9;
            } else if (evilRemains > 0) {
                board.evilRemains = evilRemains - 1;
            }
        }
        return board;
    }

    @Getter
    private String boardString;

    @Setter
    @Getter
    private int evilRemains;

    @Setter
    @Getter
    private int stonesRemains;

    @Getter
    private BoardElement[][] board;

    @Getter
    public int size;

    @Getter
    public boolean lastSnake;

    public BoardPoint getMyHead() {
        return findFirstElement(HEAD_DEAD, HEAD_DOWN, HEAD_UP, HEAD_LEFT, HEAD_RIGHT, HEAD_EVIL,
                HEAD_FLY, HEAD_SLEEP);
    }

    public List<BoardPoint> getWalls() {
        return findAllElements(WALL);
    }

    public List<BoardPoint> getStones() {
        return findAllElements(STONE);
    }

    public boolean isBarrierAt(BoardPoint point) {
        return getBarriers().contains(point);
    }

    public List<BoardPoint> getApples() {
        return findAllElements(APPLE);
    }

    public boolean amIEvil() {
        return findAllElements(HEAD_EVIL).contains(getMyHead());
    }

    public boolean amIFlying() {
        return findAllElements(HEAD_FLY).contains(getMyHead());
    }

    public List<BoardPoint> getFlyingPills() {
        return findAllElements(FLYING_PILL);
    }

    public List<BoardPoint> getFuryPills() {
        return findAllElements(FURY_PILL);
    }

    public List<BoardPoint> getGold() {
        return findAllElements(GOLD);
    }

    public List<BoardPoint> getStartPoints() {
        return findAllElements(START_FLOOR);
    }

    private List<BoardPoint> getBarriers() {
        return findAllElements(WALL, START_FLOOR, ENEMY_HEAD_SLEEP, ENEMY_TAIL_INACTIVE, TAIL_INACTIVE, STONE);
    }

    public boolean hasElementAt(BoardPoint point, BoardElement element) {
        if (point.isOutOfBoard(getSize())) {
            return false;
        }

        return getElementAt(point) == element;
    }

    public BoardElement getElementAt(BoardPoint point) {
        return board[point.getX()][point.getY()];
    }

    public void printBoard() {
        System.out.println(prettyBoardString);
    }

    public BoardPoint findElement(BoardElement elementType) {
        for (int i = 0; i < getSize() * getSize(); i++) {
            BoardPoint pt = getPointByShift(i);
            if (hasElementAt(pt, elementType)) {
                return pt;
            }
        }
        return null;
    }

    public BoardPoint findFirstElement(BoardElement... elementType) {
        for (int i = 0; i < getSize() * getSize(); i++) {
            BoardPoint pt = getPointByShift(i);

            for (BoardElement elemType : elementType) {
                if (hasElementAt(pt, elemType)) {
                    return pt;
                }
            }
        }
        return null;
    }

    public List<BoardPoint> findAllElements(BoardElement... elementType) {
        List<BoardPoint> result = new ArrayList<>();

        for (int i = 0; i < getSize() * getSize(); i++) {
            BoardPoint pt = getPointByShift(i);

            for (BoardElement elemType : elementType) {
                if (hasElementAt(pt, elemType)) {
                    result.add(pt);
                }
            }
        }

        return result;
    }

    public boolean hasElementAt(BoardPoint point, BoardElement... elements) {
        return Arrays.stream(elements).anyMatch(element -> hasElementAt(point, element));
    }

    private int getShiftByPoint(BoardPoint point) {
        return point.getY() * getSize() + point.getX();
    }

    private BoardPoint getPointByShift(int shift) {
        return new BoardPoint(shift % getSize(), shift / getSize());
    }

    public void planTo(SnakeAction action) {
        BoardPoint head = getMyHead();
        if (head != null && action.getDirection() != null) {
            BoardPoint point = action.getDirection().move(head);
            futureHead = point;
            futureElement = getElementAt(point);
            if (action.isAct()) {
                act();
            }
        }
        System.out.println("H-" + head);
    }

    public static class ElementPoint {
        private final BoardPoint boardPoint;
        private final BoardElement boardElement;

        public ElementPoint(BoardPoint boardPoint, BoardElement boardElement) {
            this.boardPoint = boardPoint;
            this.boardElement = boardElement;
        }

        @Override
        public String toString() {
            return "ElementPoint{" +
                    "boardPoint=" + boardPoint +
                    ", boardElement=" + boardElement +
                    '}';
        }
    }

    private ElementPoint getPointByShift(int shift, BoardElement boardElement) {
        return new ElementPoint(new BoardPoint(shift % getSize(), shift / getSize()), boardElement);
    }

    private Map<BoardElement, List<BoardPoint>> byElem = new HashMap<>();
    private SortedMap<Long, List<BoardPoint>> byDistance = new TreeMap<>();
    private SortedMap<Long, List<BoardPoint>> byDistanceEnemyTargets = new TreeMap<>();
    private SortedMap<Long, List<BoardPoint>> byDistanceStones = new TreeMap<>();
    private int mySnakeLength = 1;

    public int getMySnakeLength() {
        return mySnakeLength;
    }

    public void recalculate(BoardPoint head) {
        mySnakeLength = 1;
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                // строчку в тип
                BoardElement boardElement = this.board[x][y];
                ElementPoint extPoint = new ElementPoint(new BoardPoint(x, y), boardElement);

                // на всякий случай проверим, что наша голова на месте
                if (head.equals(extPoint.boardPoint) && !getMyHead().equals(extPoint.boardPoint)) {
                    throw new IllegalStateException("Should not happen");
                }

                // если наше тело, то увеличим длину.
                if (isMyBody(boardElement)) {
                    mySnakeLength++;
                }
                // чтобы искать яблоки и тп.
                byElem.computeIfAbsent(boardElement, k -> new ArrayList<>()).add(extPoint.boardPoint);
                // для значимых считаем distance и храним точку где.
                if (boardElement == APPLE
                        || boardElement == GOLD
                        || boardElement == FURY_PILL) {

                    BoardPoint boardPoint = extPoint.boardPoint;
                    if (!isDeadEnd(boardPoint, evilRemains )) {
                        byDistance.computeIfAbsent(distance(head, boardPoint), k -> new ArrayList<>())
                                .add(extPoint.boardPoint);
                    }
                }

                // just enemy body.
                if (isEnemyBody(boardElement)) {
                    byDistanceEnemyTargets.computeIfAbsent(distance(head, extPoint.boardPoint), k -> new ArrayList<>())
                            .add(extPoint.boardPoint);
                }
                //
                if (isNotEvilEnemyHead(boardElement)) {
                    // только не злых добавляем, enemy target используются только в evil mode
                    byDistanceEnemyTargets.computeIfAbsent(distance(head, extPoint.boardPoint), k -> new ArrayList<>())
                            .add(extPoint.boardPoint);
                }
                // just stones
                if (isStone(boardElement)) {
                    byDistanceStones.computeIfAbsent(distance(head, extPoint.boardPoint), k -> new ArrayList<>())
                            .add(extPoint.boardPoint);
                }

                // соберём незлых змеек от головы
                Direction notEvilNeckDirection = enemyNeck(boardElement);
                if (notEvilNeckDirection != null) {
                    notEvilSnakes.add(findSnake(extPoint, notEvilNeckDirection));
                }
                // злые змейки
                if (boardElement == ENEMY_HEAD_EVIL) {
                    Direction direction = findEvilNeckDirection(extPoint.boardPoint);
                    if (direction != null) {
                        evilSnakes.add(findSnake(extPoint, direction));
                    }
                }
            }
        }

        int enemiesLength = 0;
        int enemies = 0;
        for (Snake snake : evilSnakes) {
            int length = snake.getLength();
            enemies++;
            enemiesLength += length;
            if (length > maxEnemyLength) {
                maxEnemyLength = length;
            }
        }
        for (Snake snake : notEvilSnakes) {
            int length = snake.getLength();
            enemies++;
            enemiesLength += length;
            if (length > maxEnemyLength) {
                maxEnemyLength = length;
            }
        }
        averageEnemyLength = enemies == 0 ? 0 : enemiesLength / enemies;

        findPaths();
    }

    private Snake findSnake(ElementPoint extPoint, Direction direction) {
        Direction currDirection = direction;
        Snake snake = new Snake(extPoint);
        // пока не хвост
        BoardPoint next = extPoint.boardPoint;
        while (currDirection != null) {
            next = currDirection.move(next);
            // новая часть
            BoardElement snakeBody = board[next.getX()][next.getY()];
            if (snake.add(new ElementPoint(next, snakeBody))) {
                currDirection = nextDirection(snakeBody, currDirection);
            } else {
                System.err.printf("ВНИМАНИЕ! Зацикливание на %s %s (так не должно) \n", snakeBody.name(), next);
                currDirection = null;
            }
        }
        return snake;
    }

    private Direction findEvilNeckDirection(BoardPoint boardPoint) {
        return Stream.of(Direction.DOWN, Direction.RIGHT, Direction.UP, Direction.LEFT)
                .filter(d -> checkBoardElement(boardPoint, d))
                .findFirst()
                .orElse(null);
    }

    private boolean checkBoardElement(BoardPoint boardPoint, Direction d) {
        BoardPoint p = d.move(boardPoint);
        if (p.isOutOfBoard(size)) {
            return false;
        }

        BoardElement boardElement = board[p.getX()][p.getY()];
        boolean isBody = isEnemyBody(boardElement);
        if (!isBody && !isEnemyTail(boardElement)) {
            return false;
        }

        switch (d) {
            case DOWN:
                return (isBody
                        && (boardElement.name().contains(Direction.UP.name()) || boardElement == ENEMY_BODY_VERTICAL))
                        || (boardElement == ENEMY_TAIL_END_DOWN);
            case UP:
                return (isBody
                        && (boardElement.name().contains(Direction.DOWN.name()) || boardElement == ENEMY_BODY_VERTICAL))
                        || (boardElement == ENEMY_TAIL_END_UP);
            case LEFT:
                return (isBody
                        && (boardElement.name().contains(Direction.RIGHT.name()) || boardElement == ENEMY_BODY_HORIZONTAL))
                        || (boardElement == ENEMY_TAIL_END_LEFT);
            case RIGHT:
                return (isBody && (boardElement.name().contains(Direction.LEFT.name()) || boardElement == ENEMY_BODY_HORIZONTAL))
                        || (boardElement == ENEMY_TAIL_END_RIGHT);
        }

        return false;
    }

    @Getter
    List<Snake> notEvilSnakes = new ArrayList<>();
    @Getter
    List<Snake> evilSnakes = new ArrayList<>();

    @Getter
    int maxEnemyLength;

    @Getter
    int averageEnemyLength;

    private Direction nextDirection(BoardElement snakeBody, Direction direction) {
        switch (snakeBody) {
            case ENEMY_BODY_HORIZONTAL: // ('─')
            case ENEMY_BODY_VERTICAL: {
                //('│'),

                return direction;
            }
            case ENEMY_BODY_LEFT_DOWN: {
                //('┐'),
                if (direction == Direction.UP) {
                    return Direction.LEFT;
                }
                return Direction.DOWN;
            }
            case ENEMY_BODY_LEFT_UP: {
                //('┘'),
                if (direction == Direction.DOWN) {
                    return Direction.LEFT;
                }
                return Direction.UP;
            }
            case ENEMY_BODY_RIGHT_DOWN: {
                //('┌'),
                if (direction == Direction.UP) {
                    return Direction.RIGHT;
                }
                return Direction.DOWN;
            }
            case ENEMY_BODY_RIGHT_UP: {
                //('└'),
                if (direction == Direction.DOWN) {
                    return Direction.RIGHT;
                }
                return Direction.UP;
            }
            default: // tail
                return null;
        }
    }

    public static class Snake {

        private final List<ElementPoint> body = new ArrayList<>();
        private final List<BoardPoint> bodyPoints = new ArrayList<>();

        Snake(ElementPoint head) {
            this.body.add(head);
            bodyPoints.add(head.boardPoint);
        }

        public boolean add(ElementPoint elementPoint) {
            if (body.contains(elementPoint)) {
                return false;
            }
            this.body.add(elementPoint);
            this.bodyPoints.add(elementPoint.boardPoint);
            return true;
        }

        public List<ElementPoint> getBody() {
            return body;
        }

        public boolean isSnakePoint(BoardPoint point, int afterSteps) {
            int pointIndex = bodyPoints.indexOf(point);
            if (pointIndex < 0) {
                return false;
            } else {
                return bodyPoints.size() > pointIndex + afterSteps;
            }
        }

        public boolean isHeadOrNeck(BoardPoint point, int afterSteps) {
            if (afterSteps > 1) {
                return false;
            } else if (afterSteps == 1) {
                return bodyPoints.get(0).equals(point);
            }
            throw new IllegalArgumentException("expected at least one step");
        }

        public int costIfWin(BoardPoint attackPoint, int afterSteps) {
            int headPart = bodyPoints.indexOf(attackPoint) + afterSteps;
            if (headPart < 2) {
                return bodyPoints.size() * Prices.SNAKE_SEGMENT_COST;
            } else {
                return (bodyPoints.size() - headPart) * Prices.SNAKE_SEGMENT_COST;
            }
        }

        public List<BoardPoint> getBodyPoints() {
            return bodyPoints;
        }

        @Override
        public String toString() {
            return "Snake{" +
                    "body=" + body +
                    '}';
        }

        public int getLength() {
            return bodyPoints.size();
        }
    }

    private Direction enemyNeck(BoardElement boardElement) {
        switch (boardElement) {
            case ENEMY_HEAD_DOWN:
                return Direction.UP;
            case ENEMY_HEAD_UP:
                return Direction.DOWN;
            case ENEMY_HEAD_LEFT:
                return Direction.RIGHT;
            case ENEMY_HEAD_RIGHT:
                return Direction.LEFT;
            default:
                return null;
        }
    }

    public void act() {
        if (stonesRemains > 0) {
            stonesRemains--;
        }
    }

    private boolean isEnemyHeadEvil(BoardElement boardElement) {
        return ENEMY_HEAD_EVIL == boardElement;
    }


    private boolean isDeadEnd(BoardPoint boardPoint, int evilTurns) {
        boolean evil = evilTurns > 0;
        return Arrays.stream(Direction.values())
                .map(d -> d.move(boardPoint))
                .filter(bp -> bp.isOutOfBoard(size) || isExactlyBad(getElementAt(bp), evil))
                .count() >= 3;
    }

    @Getter
    List<ElemStat> goodThings;

    private void findPaths() {
        goodThings = new ArrayList<>();
        SortedSet<ElemStat> front = new TreeSet<>(
                Comparator
                        .comparingLong(ElemStat::getDistance)
                        .thenComparing(elemStat -> elemStat.point.getX())
                        .thenComparing(elemStat -> elemStat.point.getY())
        );
        ElemStat[][] marked = new ElemStat[size][size];

        BoardPoint head = getMyHead();
        val headStat = new ElemStat(
                0,
                0,
                getElementAt(head),
                null,
                head,
                0,
                null,
                null
        );
        front.add(headStat);
        marked[head.getX()][head.getY()] = headStat;

        while (!front.isEmpty()) {
            ElemStat e = front.first();
            front.remove(e);

            Arrays.asList(Direction.RIGHT, Direction.DOWN, Direction.LEFT, Direction.UP).forEach(direction -> {
                BoardPoint newPoint = direction.move(e.point);
                if (newPoint.isOutOfBoard(size)) {
                    return;
                }
                ElemStat prevVisit = marked[newPoint.getX()][newPoint.getY()];
                if (prevVisit != null && (prevVisit.distance <= e.distance || prevVisit.prevPathCost >= e.cost + e.prevPathCost)) {
                    return;
                }
                BoardElement element = getElementAt(newPoint);
                EnumSet<BoardElement> acceptable = EnumSet.of(APPLE, GOLD, FURY_PILL, NONE);
                if (e.distance < evilRemains || mySnakeLength >= 50) {
                    acceptable.add(STONE);
                }
                acceptable.addAll(
                        Arrays.asList(
                                ENEMY_TAIL_END_DOWN,
                                ENEMY_TAIL_END_LEFT,
                                ENEMY_TAIL_END_UP,
                                ENEMY_TAIL_END_RIGHT,

                                ENEMY_BODY_VERTICAL,
                                ENEMY_BODY_LEFT_DOWN,
                                ENEMY_BODY_LEFT_UP,
                                ENEMY_BODY_RIGHT_DOWN,
                                ENEMY_BODY_RIGHT_UP,
                                ENEMY_BODY_HORIZONTAL,

                                ENEMY_HEAD_DOWN,
                                ENEMY_HEAD_LEFT,
                                ENEMY_HEAD_RIGHT,
                                ENEMY_HEAD_UP,

                                ENEMY_HEAD_EVIL
                        ));
                if (acceptable.contains(element)) {
                    int newDistance = e.distance + 1;

                    // не ходим по умолчанию никуда.
                    int cost = -1;
                    Snake evilSnake = null;
                    Snake nonEvilSnake = null;

                    switch (element) {
                        case APPLE:
                            cost = Prices.APPLE_COST;
                            break;
                        case STONE:
                            cost = Prices.STONE_COST;
                            break;
                        case GOLD:
                            cost = Prices.GOLD_COST;
                            break;
                        case FURY_PILL:
                            cost = Prices.FURY_PILL_COST; //?
                            break;
                        case NONE:
                            cost = 0;
                            break;
                        default:
                            // дуэль
                            lastSnake = evilSnakes.size() + notEvilSnakes.size() == 1;
                            // enemy
                            evilSnake = evilSnakes.stream()
                                    .filter(snake -> snake.isSnakePoint(newPoint, newDistance))
                                    .findFirst()
                                    .orElse(null);

                            if (evilSnake != null) {
                                // мы злы
                                if (e.distance < evilRemains) {
                                    // враг зол
                                    if (evilSnake.isHeadOrNeck(newPoint, newDistance)) {
                                        // мы длиннее
                                        if (mySnakeLength - 2 > evilSnake.bodyPoints.size()) {
                                            cost = evilSnake.body.size() * Prices.SNAKE_SEGMENT_COST;

                                            // но, если это последняя змея.
                                            if (lastSnake) {
                                                // чем больше разница между нами и той змеёй, тем привлекательней она для нас.
                                                // рискнём.
                                                // если мы приблизительно такие или приблизительно такие - вперёд!
                                                cost = (mySnakeLength - evilSnake.bodyPoints.size()) * cost;
                                            }
                                        } else {
                                            cost = -1;
                                        }
                                    } else { // кусь в пузико
                                        cost = evilSnake.costIfWin(newPoint, newDistance);
                                        // если это последняя змея и мы длиннне то ещё сильнее кусь
                                        if (lastSnake && mySnakeLength - 2 > nonEvilSnake.bodyPoints.size()) {
                                            // последняя змея особо привлекательна
                                            cost = (mySnakeLength - nonEvilSnake.bodyPoints.size()) * cost;
                                        }
                                    }
                                } else {
                                    cost = -1;
                                }
                            }
                            nonEvilSnake = notEvilSnakes.stream()
                                    .filter(snake -> snake.isSnakePoint(newPoint, newDistance))
                                    .findFirst()
                                    .orElse(null);

                            if (nonEvilSnake == null) { // enemy already dead
//                                cost = 0;
                            } else {
                                // если мы ещё злы!
                                if (e.distance < evilRemains) {
                                    cost = nonEvilSnake.costIfWin(newPoint, newDistance);
                                    // рискнём, если мы последние.
                                    // если мы приблизительно такие или приблизительно такие - вперёд!
                                    if (lastSnake && mySnakeLength >= nonEvilSnake.bodyPoints.size()) {
                                        cost = (mySnakeLength - nonEvilSnake.bodyPoints.size() + 1) * cost;
                                    }
                                } else {
                                    if (nonEvilSnake.isHeadOrNeck(newPoint, newDistance)) {
                                        if (mySnakeLength - 2 > nonEvilSnake.bodyPoints.size()) {
                                            cost = nonEvilSnake.body.size() * Prices.SNAKE_SEGMENT_COST;
                                            // даже, если не последняя змейка, но нас больше - вперёд!
                                            // чем больше разница между нами и той змеёй, тем привлекательней она для нас.
                                            cost = ((mySnakeLength - 2) - nonEvilSnake.bodyPoints.size()) * cost;
                                        } else {
                                            cost = -1;
                                        }
                                    } else { // кусь в пузико
                                        cost = -1;
                                    }
                                }
                            }
                    }
                    if (cost >= 0) {
                        ElemStat elem = new ElemStat(
                                newDistance,
                                e.prevPathCost + e.cost,
                                element,
                                e.direction == null ? direction : e.direction,
                                newPoint,
                                cost,
                                evilSnake,
                                nonEvilSnake
                        );
                        marked[newPoint.getX()][newPoint.getY()] = elem;
                        front.add(elem);
                    }
                }
            });
        }

        for (ElemStat[] elemStats : marked) {
            for (ElemStat elemStat : elemStats) {
                if (elemStat != null) {
                    if (EnumSet.of(APPLE, GOLD, FURY_PILL, STONE, ENEMY_TAIL_END_DOWN,
                            ENEMY_TAIL_END_LEFT,
                            ENEMY_TAIL_END_UP,
                            ENEMY_TAIL_END_RIGHT,

                            ENEMY_BODY_VERTICAL,
                            ENEMY_BODY_LEFT_DOWN,
                            ENEMY_BODY_LEFT_UP,
                            ENEMY_BODY_RIGHT_DOWN,
                            ENEMY_BODY_RIGHT_UP,
                            ENEMY_BODY_HORIZONTAL,

                            ENEMY_HEAD_DOWN,
                            ENEMY_HEAD_LEFT,
                            ENEMY_HEAD_RIGHT,
                            ENEMY_HEAD_UP).contains(elemStat.type)) {
                        if (!isDeadEnd(elemStat.point, 0)) {
                            goodThings.add(elemStat);
                        }
                    }
                }
            }
        }
        goodThings.sort(Comparator.comparingDouble(ElemStat::rate).reversed());
    }

    @ToString
    @AllArgsConstructor
    public static class ElemStat {
        @Getter
        int distance;
        @Getter
        int prevPathCost;
        @Getter
        BoardElement type;
        @Getter
        Direction direction;
        @Getter
        BoardPoint point;
        @Getter
        int cost;
        @Getter
        Snake evilSnake;
        @Getter
        Snake nonEvilSnake;

        public double rate() {
            return cost * 1.0 / distance;
        }
    }

    private boolean isExactlyBad(BoardElement element, boolean evil) {
        switch (element) {
            case STONE:
                return !evil;
            case START_FLOOR:
            case WALL:
            case ENEMY_HEAD_SLEEP:
            case ENEMY_TAIL_INACTIVE:
            case TAIL_INACTIVE:
                return true;
            default:
                return false;
        }
    }

    public boolean isNotEvilEnemyHead(BoardElement boardElement) {
        return boardElement == BoardElement.ENEMY_HEAD_DOWN
                || boardElement == BoardElement.ENEMY_HEAD_LEFT
                || boardElement == BoardElement.ENEMY_HEAD_RIGHT
                || boardElement == BoardElement.ENEMY_HEAD_UP;
    }

    public boolean isEnemyBody(BoardElement boardElement) {
        return boardElement == ENEMY_BODY_HORIZONTAL
                || boardElement == ENEMY_BODY_LEFT_DOWN
                || boardElement == ENEMY_BODY_LEFT_UP
                || boardElement == ENEMY_BODY_RIGHT_DOWN
                || boardElement == ENEMY_BODY_RIGHT_UP
                || boardElement == ENEMY_BODY_HORIZONTAL
                || boardElement == ENEMY_BODY_VERTICAL;
    }

    public boolean isEnemyTail(BoardElement boardElement) {
        return boardElement == ENEMY_TAIL_END_DOWN
                || boardElement == ENEMY_TAIL_END_UP
                || boardElement == ENEMY_TAIL_END_LEFT
                || boardElement == ENEMY_TAIL_END_RIGHT;
    }

    public boolean isStone(BoardElement boardElement) {
        return boardElement == STONE;
    }

    public boolean isMyBody(BoardElement boardElement) {
        return boardElement == BODY_HORIZONTAL
                || boardElement == BODY_LEFT_DOWN
                || boardElement == BODY_LEFT_UP
                || boardElement == BODY_HORIZONTAL
                || boardElement == BODY_HORIZONTAL
                || boardElement == BODY_VERTICAL
                || boardElement == BODY_RIGHT_DOWN
                || boardElement == BODY_RIGHT_UP;
    }

    public long distance(BoardPoint myHead, BoardPoint currPoint) {
        return Math.abs(myHead.getX() - currPoint.getX()) + Math.abs(myHead.getY() - currPoint.getY());
    }

    public SortedMap<Long, List<BoardPoint>> getByDistanceMap() {
        return byDistance;
    }

    public SortedMap<Long, List<BoardPoint>> getByDistanceEnemyTargets() {
        return byDistanceEnemyTargets;
    }

    public SortedMap<Long, List<BoardPoint>> getByDistanceStones() {
        return byDistanceStones;
    }
}
