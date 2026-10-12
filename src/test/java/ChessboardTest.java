import java.util.ArrayList;
import java.util.List;

public class ChessboardTest {

    private static int passCount = 0;
    private static int failCount = 0;

    public static void main(String[] args) {
        // 基础落子
        testPlaceNormal();
        testPlaceOccupied();
        testPlaceOutOfBounds();
        testPlaceNull();
        testPlaceEmpty();

        // 边界检查
        testIsInBounds();

        // RuleEngine 基础检测
        testHorizontalWin();
        testVerticalWin();
        testDiagonalDownWin();
        testDiagonalUpWin();
        testNoWin();

        // 极端情况
        testEdgeRowWin();
        testCornerWin();
        testLongLine();
        testCrossDoubleLine();
        testFourLines();

        // removeAll
        testRemoveAllBasic();
        testRemoveAllDuplicates();
        testRemoveAllInvalid();

        // 快照恢复
        testSnapshotRestore();

        System.out.println("\n========== 测试结果 ==========");
        System.out.println("通过: " + passCount);
        System.out.println("失败: " + failCount);
    }

    // ==================== 基础落子 ====================

    private static void testPlaceNormal() {
        System.out.println("\n=== 测试 正常落子 ===");
        Chessboard board = new Chessboard();
        assertTrue("空位落黑子", board.place(7, 7, Piece.BLACK));
        assertEquals("(7,7)应为黑子", Piece.BLACK, board.getPiece(7, 7));
        assertTrue("(7,7)不空", !board.isEmpty(7, 7));
    }

    private static void testPlaceOccupied() {
        System.out.println("\n=== 测试 落子在已有棋子上 ===");
        Chessboard board = new Chessboard();
        board.place(7, 7, Piece.BLACK);
        assertTrue("(7,7)已有子，落白子应失败", !board.place(7, 7, Piece.WHITE));
    }

    private static void testPlaceOutOfBounds() {
        System.out.println("\n=== 测试 越界落子 ===");
        Chessboard board = new Chessboard();
        assertTrue("(-1,0)越界", !board.place(-1, 0, Piece.BLACK));
        assertTrue("(0,-1)越界", !board.place(0, -1, Piece.BLACK));
        assertTrue("(15,0)越界", !board.place(15, 0, Piece.BLACK));
        assertTrue("(0,15)越界", !board.place(0, 15, Piece.BLACK));
    }

    private static void testPlaceNull() {
        System.out.println("\n=== 测试 落子为null ===");
        Chessboard board = new Chessboard();
        assertTrue("null应拒绝", !board.place(7, 7, null));
    }

    private static void testPlaceEmpty() {
        System.out.println("\n=== 测试 落子为EMPTY ===");
        Chessboard board = new Chessboard();
        assertTrue("EMPTY应拒绝", !board.place(7, 7, Piece.EMPTY));
    }

    // ==================== 边界检查 ====================

    private static void testIsInBounds() {
        System.out.println("\n=== 测试 isInBounds 边界 ===");
        Chessboard board = new Chessboard();
        assertTrue("(0,0)在界内", board.isInBounds(0, 0));
        assertTrue("(14,14)在界内", board.isInBounds(14, 14));
        assertTrue("(-1,0)越界", !board.isInBounds(-1, 0));
        assertTrue("(15,0)越界", !board.isInBounds(15, 0));
        assertTrue("(0,-1)越界", !board.isInBounds(0, -1));
        assertTrue("(0,15)越界", !board.isInBounds(0, 15));
    }

    // ==================== RuleEngine 基础 ====================

    private static void testHorizontalWin() {
        System.out.println("\n=== 测试 横向5连 ===");
        Chessboard board = new Chessboard();
        for (int col = 3; col <= 7; col++) board.place(7, col, Piece.BLACK);

        RuleEngine engine = new RuleEngine();
        List<List<int[]>> lines = engine.detectAllLines(board, 7, 7, Piece.BLACK);

        assertEquals("横向5连应返回1条线", 1, lines.size());
        assertEquals("该线应有5颗", 5, lines.get(0).size());
    }

    private static void testVerticalWin() {
        System.out.println("\n=== 测试 竖向5连 ===");
        Chessboard board = new Chessboard();
        for (int row = 3; row <= 7; row++) board.place(row, 7, Piece.WHITE);

        RuleEngine engine = new RuleEngine();
        List<List<int[]>> lines = engine.detectAllLines(board, 7, 7, Piece.WHITE);

        assertEquals("竖向5连应返回1条线", 1, lines.size());
        assertEquals("该线应有5颗", 5, lines.get(0).size());
    }

    private static void testDiagonalDownWin() {
        System.out.println("\n=== 测试 正斜5连（↘） ===");
        Chessboard board = new Chessboard();
        for (int i = 0; i < 5; i++) board.place(3 + i, 3 + i, Piece.BLACK);

        RuleEngine engine = new RuleEngine();
        List<List<int[]>> lines = engine.detectAllLines(board, 7, 7, Piece.BLACK);

        assertEquals("正斜5连应返回1条线", 1, lines.size());
        assertEquals("该线应有5颗", 5, lines.get(0).size());
    }

    private static void testDiagonalUpWin() {
        System.out.println("\n=== 测试 反斜5连（↙） ===");
        Chessboard board = new Chessboard();
        for (int i = 0; i < 5; i++) board.place(3 + i, 11 - i, Piece.WHITE);

        RuleEngine engine = new RuleEngine();
        List<List<int[]>> lines = engine.detectAllLines(board, 7, 7, Piece.WHITE);

        assertEquals("反斜5连应返回1条线", 1, lines.size());
        assertEquals("该线应有5颗", 5, lines.get(0).size());
    }

    private static void testNoWin() {
        System.out.println("\n=== 测试 只有4颗（不应连珠） ===");
        Chessboard board = new Chessboard();
        for (int col = 3; col <= 6; col++) board.place(7, col, Piece.BLACK);

        RuleEngine engine = new RuleEngine();
        List<List<int[]>> lines = engine.detectAllLines(board, 7, 6, Piece.BLACK);

        assertEquals("4颗不应连珠", 0, lines.size());
    }

    // ==================== 极端情况 ====================

    private static void testEdgeRowWin() {
        System.out.println("\n=== 极端 棋盘第0行横5连 ===");
        Chessboard board = new Chessboard();
        for (int col = 0; col <= 4; col++) board.place(0, col, Piece.BLACK);

        RuleEngine engine = new RuleEngine();
        List<List<int[]>> lines = engine.detectAllLines(board, 0, 4, Piece.BLACK);

        assertEquals("第0行横5连", 1, lines.size());
        assertEquals("5颗", 5, lines.get(0).size());
    }

    private static void testCornerWin() {
        System.out.println("\n=== 极端 右下角斜5连 ===");
        Chessboard board = new Chessboard();
        // 从 (10,10) 到 (14,14) 正斜
        for (int i = 0; i < 5; i++) board.place(10 + i, 10 + i, Piece.WHITE);

        RuleEngine engine = new RuleEngine();
        List<List<int[]>> lines = engine.detectAllLines(board, 14, 14, Piece.WHITE);

        assertEquals("右下角斜5连", 1, lines.size());
        assertEquals("5颗", 5, lines.get(0).size());
    }

    private static void testLongLine() {
        System.out.println("\n=== 极端 7连长连（应全部返回） ===");
        Chessboard board = new Chessboard();
        for (int col = 3; col <= 9; col++) board.place(7, col, Piece.BLACK);

        RuleEngine engine = new RuleEngine();
        List<List<int[]>> lines = engine.detectAllLines(board, 7, 9, Piece.BLACK);

        assertEquals("7连应返回1条线", 1, lines.size());
        assertEquals("该线应有7颗（不截取）", 7, lines.get(0).size());
    }

    private static void testCrossDoubleLine() {
        System.out.println("\n=== 极端 十字双线 ===");
        Chessboard board = new Chessboard();
        // 横向：(7,3)~(7,6) + 落子(7,7) = 5连
        for (int col = 3; col <= 6; col++) board.place(7, col, Piece.BLACK);
        // 竖向：(3,7)~(6,7) + 落子(7,7) = 5连
        for (int row = 3; row <= 6; row++) board.place(row, 7, Piece.BLACK);

        board.place(7, 7, Piece.BLACK);

        RuleEngine engine = new RuleEngine();
        List<List<int[]>> lines = engine.detectAllLines(board, 7, 7, Piece.BLACK);

        assertEquals("十字应返回2条线", 2, lines.size());
        assertEquals("每条线5颗", 5, lines.get(0).size());
        assertEquals("每条线5颗", 5, lines.get(1).size());

        // 合并坐标 + 消除
        List<int[]> all = new ArrayList<>();
        for (List<int[]> line : lines) all.addAll(line);
        assertEquals("合并后坐标数=10（中心点重复1次）", 10, all.size());

        int removed = board.removeAll(all);
        assertEquals("实际消除9颗（中心点去重）", 9, removed);
    }

    private static void testFourLines() {
        System.out.println("\n=== 极端 四四（一子四线） ===");
        Chessboard board = new Chessboard();

        // 横线：左右各4颗
        for (int col = 3; col <= 6; col++) board.place(7, col, Piece.BLACK);
        for (int col = 8; col <= 11; col++) board.place(7, col, Piece.BLACK);
        // 竖线：上下各4颗
        for (int row = 3; row <= 6; row++) board.place(row, 7, Piece.BLACK);
        for (int row = 8; row <= 11; row++) board.place(row, 7, Piece.BLACK);
        // 正斜：左上右下各4颗
        for (int i = 3; i <= 6; i++) board.place(i, i, Piece.BLACK);
        for (int i = 8; i <= 11; i++) board.place(i, i, Piece.BLACK);
        // 反斜：右上左下各4颗
        for (int i = 3; i <= 6; i++) board.place(i, 14 - i, Piece.BLACK);
        for (int i = 8; i <= 11; i++) board.place(i, 14 - i, Piece.BLACK);

        // 中心落子
        board.place(7, 7, Piece.BLACK);

        RuleEngine engine = new RuleEngine();
        List<List<int[]>> lines = engine.detectAllLines(board, 7, 7, Piece.BLACK);

        assertEquals("四四应返回4条线", 4, lines.size());
        for (List<int[]> line : lines) {
            assertEquals("每条线9颗", 9, line.size());
        }

        // 合并坐标
        List<int[]> all = new ArrayList<>();
        for (List<int[]> line : lines) all.addAll(line);
        assertEquals("合并后36个坐标", 36, all.size());

        int removed = board.removeAll(all);
        assertEquals("实际消除33颗（中心点4次去重为1次）", 33, removed);
    }

    // ==================== removeAll ====================

    private static void testRemoveAllBasic() {
        System.out.println("\n=== 测试 removeAll 基础 ===");
        Chessboard board = new Chessboard();
        board.place(7, 7, Piece.BLACK);
        board.place(7, 8, Piece.BLACK);

        List<int[]> positions = new ArrayList<>();
        positions.add(new int[]{7, 7});
        positions.add(new int[]{7, 8});

        int removed = board.removeAll(positions);
        assertEquals("消除2颗", 2, removed);
        assertTrue("(7,7)已空", board.isEmpty(7, 7));
        assertTrue("(7,8)已空", board.isEmpty(7, 8));
    }

    private static void testRemoveAllDuplicates() {
        System.out.println("\n=== 测试 removeAll 去重 ===");
        Chessboard board = new Chessboard();
        board.place(7, 7, Piece.BLACK);

        List<int[]> positions = new ArrayList<>();
        positions.add(new int[]{7, 7});
        positions.add(new int[]{7, 7});  // 故意重复
        positions.add(new int[]{7, 7});  // 再来一次

        int removed = board.removeAll(positions);
        assertEquals("重复坐标只消除1颗", 1, removed);
    }

    private static void testRemoveAllInvalid() {
        System.out.println("\n=== 测试 removeAll 非法输入 ===");
        Chessboard board = new Chessboard();
        board.place(7, 7, Piece.BLACK);

        // null列表
        assertEquals("null列表返回0", 0, board.removeAll(null));

        // 空列表
        assertEquals("空列表返回0", 0, board.removeAll(new ArrayList<>()));

        // 含非法元素
        List<int[]> positions = new ArrayList<>();
        positions.add(null);                    // null
        positions.add(new int[]{7});            // 长度1
        positions.add(new int[]{7, 7, 8});      // 长度3
        positions.add(new int[]{-1, 0});        // 越界
        positions.add(new int[]{100, 100});     // 越界
        positions.add(new int[]{0, 0});         // 空位
        positions.add(new int[]{7, 7});         // 合法

        int removed = board.removeAll(positions);
        assertEquals("只消除合法的1颗", 1, removed);
        assertTrue("(7,7)已空", board.isEmpty(7, 7));
    }

    // ==================== 快照恢复 ====================

    private static void testSnapshotRestore() {
        System.out.println("\n=== 测试 快照/恢复 ===");
        Chessboard board = new Chessboard();
        board.place(7, 7, Piece.BLACK);
        board.place(7, 8, Piece.WHITE);

        // 快照
        Piece[][] snap = board.snapshot();

        // 修改棋盘
        board.place(8, 8, Piece.BLACK);
        board.remove(7, 7);

        // 恢复
        assertTrue("恢复成功", board.restore(snap));
        assertEquals("(7,7)恢复为黑", Piece.BLACK, board.getPiece(7, 7));
        assertEquals("(7,8)恢复为白", Piece.WHITE, board.getPiece(7, 8));
        assertEquals("(8,8)恢复为空", Piece.EMPTY, board.getPiece(8, 8));

        // 验证快照独立
        board.place(9, 9, Piece.BLACK);
        assertEquals("快照不受棋盘影响", Piece.EMPTY, snap[9][9]);
    }

    // ==================== 断言工具 ====================

    private static void assertTrue(String msg, boolean condition) {
        if (condition) {
            System.out.println("[PASS] " + msg);
            passCount++;
        } else {
            System.out.println("[FAIL] " + msg);
            failCount++;
        }
    }

    private static void assertEquals(String msg, int expected, int actual) {
        if (expected == actual) {
            System.out.println("[PASS] " + msg + " (=" + actual + ")");
            passCount++;
        } else {
            System.out.println("[FAIL] " + msg + " 期望=" + expected + " 实际=" + actual);
            failCount++;
        }
    }

    private static void assertEquals(String msg, Object expected, Object actual) {
        if (expected == null ? actual == null : expected.equals(actual)) {
            System.out.println("[PASS] " + msg);
            passCount++;
        } else {
            System.out.println("[FAIL] " + msg + " 期望=" + expected + " 实际=" + actual);
            failCount++;
        }
    }
}