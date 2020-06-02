package ru.codebattle.client.api;

public class Prices {
    //    public static int APPLE_COST = 1;
//    public static int GOLD_COST = 10;
//    public static int STONE_COST = 5;
//    public static int SNAKE_SEGMENT_COST = 10;
    public static int APPLE_COST = GOLD_IN_CENTER.APPLE_COST;
    public static int GOLD_COST = GOLD_IN_CENTER.GOLD_COST;
    public static int STONE_COST = GOLD_IN_CENTER.STONE_COST;
    public static int SNAKE_SEGMENT_COST = GOLD_IN_CENTER.SNAKE_SEGMENT_COST;
    public static final int FURY_PILL_COST = GOLD_IN_CENTER.FURY_PILL_COST;

    // для текущей карты, где рельеф простой.
    // удлиняемся, если есть возможность берём ярость и производные.
    // золото не так актуально, хочется посмотреть куси.
    interface DESERT {
        int APPLE_COST = 5;
        int FURY_PILL_COST = 4;
        int GOLD_COST = 4;
        int STONE_COST = 5;
        int SNAKE_SEGMENT_COST = 10;
    }

    interface GOLD_IN_CENTER {
        int APPLE_COST = 5;
        int FURY_PILL_COST = 5;
        int GOLD_COST = 5;
        int STONE_COST = 5;
        int SNAKE_SEGMENT_COST = 100;
    }

}
