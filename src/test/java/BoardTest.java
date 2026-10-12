public class BoardTest {

    public static void main(String[] args) {
        testHorizontal();
        testVertical();
        testDiagonalDown();
        testDiagonalUp();
        testNoWin();
    }

    /**
     * 测试横向5连
     * 落子位置：(7,3) (7,4) (7,5) (7,6) (7,7)
     */
    private static void testHorizontal() {
        System.out.println("========== 测试横向5连 ==========");
        Chessboard board = new Chessboard();
        board.reset();

        for (int col = 3; col <= 7; col++) {
            board.place(7, col, Piece.BLACK);
        }

        System.out.println(board);
        System.out.println("黑棋落子(7,7)后，横向应有5颗连珠");
        System.out.println("预期：(7,3)~(7,7) 全是 X");
        System.out.println();
    }

    /**
     * 测试竖向5连
     * 落子位置：(3,7) (4,7) (5,7) (6,7) (7,7)
     */
    private static void testVertical() {
        System.out.println("========== 测试竖向5连 ==========");
        Chessboard board = new Chessboard();
        board.reset();

        for (int row = 3; row <= 7; row++) {
            board.place(row, 7, Piece.WHITE);
        }

        System.out.println(board);
        System.out.println("白棋落子(7,7)后，竖向应有5颗连珠");
        System.out.println("预期：(3,7)~(7,7) 全是 O");
        System.out.println();
    }

    /**
     * 测试正斜5连（左上到右下）
     * 落子位置：(3,3) (4,4) (5,5) (6,6) (7,7)
     */
    private static void testDiagonalDown() {
        System.out.println("========== 测试正斜5连（↘）==========");
        Chessboard board = new Chessboard();
        board.reset();

        for (int i = 0; i < 5; i++) {
            board.place(3 + i, 3 + i, Piece.BLACK);
        }

        System.out.println(board);
        System.out.println("黑棋落子(7,7)后，正斜应有5颗连珠");
        System.out.println("预期：(3,3) (4,4) (5,5) (6,6) (7,7) 全是 X");
        System.out.println();
    }

    /**
     * 测试反斜5连（右上到左下）
     * 落子位置：(3,11) (4,10) (5,9) (6,8) (7,7)
     */
    private static void testDiagonalUp() {
        System.out.println("========== 测试反斜5连（↙）==========");
        Chessboard board = new Chessboard();
        board.reset();

        for (int i = 0; i < 5; i++) {
            board.place(3 + i, 11 - i, Piece.WHITE);
        }

        System.out.println(board);
        System.out.println("白棋落子(7,7)后，反斜应有5颗连珠");
        System.out.println("预期：(3,11) (4,10) (5,9) (6,8) (7,7) 全是 O");
        System.out.println();
    }

    /**
     * 测试未形成5连的情况
     * 只有4颗连续，不应判定为胜利
     */
    private static void testNoWin() {
        System.out.println("========== 测试只有4颗（不应连珠）==========");
        Chessboard board = new Chessboard();
        board.reset();

        for (int col = 3; col <= 6; col++) {
            board.place(7, col, Piece.BLACK);
        }

        System.out.println(board);
        System.out.println("只有4颗黑棋，不应形成连珠");
        System.out.println("预期：RuleEngine 应返回空列表");
        System.out.println();
    }
}