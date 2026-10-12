import java.util.ArrayDeque;
import java.util.Deque;

public class GameState {
    private final Chessboard board; //棋盘
    private final RuleEngine ruleEngine;    //规则引擎

    //双方角色
    private final Role blackRole;
    private final Role whiteRole;

    private Piece currentPlayer;    //当前该谁下，只能是 Piece.BLACK 或 Piece.WHITE
    private int turnCount;  //回合数，从 1 开始，切换玩家后 +1
    private boolean gameOver;   //是否结束，方便 UI 和控制循环判断

    private final Deque<Piece[][]> history; //历史快照栈，用于悔棋或回滚

    private String selectedMark;   // 玩家选中的真标记坐标，null 表示未选

    /**
     * 全参构造方法
     * @param board 棋盘
     * @param ruleEngine    规则引擎
     * @param blackRole     黑棋
     * @param whiteRole     白棋
     * @param pieces        先手棋
     */
    public GameState(Chessboard board, RuleEngine ruleEngine, Role blackRole, Role whiteRole, Piece pieces) {
        //防御性编程
        if (board == null) throw new IllegalArgumentException("棋盘不能为null");
        if (ruleEngine == null) throw new IllegalArgumentException("规则引擎不能为null");
        if (blackRole == null || whiteRole == null) throw new IllegalArgumentException("角色不能为null");
        if (pieces != Piece.BLACK && pieces != Piece.WHITE) throw new IllegalArgumentException("先手棋只能是 BLACK 或 WHITE");


        this.board = board;
        this.ruleEngine = ruleEngine;
        this.blackRole = blackRole;
        this.whiteRole = whiteRole;

        //初始化
        this.currentPlayer = pieces;   //由此可决定先后手
        this.turnCount = 1;
        this.gameOver = false;
        this.history = new ArrayDeque<>();
        this.selectedMark = null;
    }

    /**
     * 常规构造，默认传入黑棋先手
     * @param board 棋盘
     * @param ruleEngine    规则引擎
     * @param blackRole     黑棋
     * @param whiteRole     白棋
     */
    public GameState(Chessboard board, RuleEngine ruleEngine, Role blackRole, Role whiteRole){
        this(board,ruleEngine,blackRole,whiteRole, Piece.BLACK);
    }

    /**
     * 切换当前玩家，并推进回合数。
     * 正常情况下只在 BLACK 和 WHITE 之间切换。
     */
    public void switchPlayer() {
        if (gameOver) {
            throw new IllegalStateException("游戏已结束，不能切换玩家");
        }
        if (currentPlayer == Piece.BLACK) {
            currentPlayer = Piece.WHITE;
        } else if (currentPlayer == Piece.WHITE) {
            currentPlayer = Piece.BLACK;
        } else {
            throw new IllegalStateException("当前玩家状态非法: " + currentPlayer);
        }
        turnCount++;
    }

    /**
     * 将棋盘快照压入快照栈
     */
    public void saveSnapshot() {
        history.push(board.snapshot());
    }

    /**
     * 返回选中目标，可能为 null
     * @return  返回主要目标坐标
     */
    public String getSelectedMark(){
        return selectedMark;
    }

    /**
     * 设置选中目标
     * @param mark  主要目标坐标(row,col)
     */
    public void setSelectedMark(String mark){
        selectedMark = mark;
    }

    /**
     * 清除选中
     */
    public void clearSelectedMark(){
        selectedMark = null;
    }


    /**
     * 返回当前回合玩家的角色对象
     * @return  返回当前回合玩家的角色对象
     */
    public Role getCurrentRole(){
        if (currentPlayer == Piece.BLACK) {
            return blackRole;
        }
        if (currentPlayer == Piece.WHITE) {
            return whiteRole;
        }
        throw new IllegalStateException("当前玩家状态非法: " + currentPlayer);
    }

    /**
     * 返回当前回合对手的角色对象
     * @return  返回对手的角色对象
     */
    public Role getOpponentRole(){
        if (currentPlayer == Piece.BLACK) {
            return whiteRole;
        }
        if (currentPlayer == Piece.WHITE) {
            return blackRole;
        }
        throw new IllegalStateException("当前玩家状态非法: " + currentPlayer);
    }

    /**
     * 按棋色获取对应角色
     * @param piece 棋色
     * @return  返回对应角色对象
     */
    public Role getRole(Piece piece){
        if (piece == Piece.BLACK) {
            return blackRole;
        }
        if (piece == Piece.WHITE) {
            return whiteRole;
        }
        throw new IllegalArgumentException("EMPTY 没有对应角色");
    }

    /**
     * 获取棋盘对象。
     *
     * @return 当前棋盘
     */
    public Chessboard getBoard() {
        return board;
    }

    /**
     * 获取规则引擎对象。
     *
     * @return 当前规则引擎
     */
    public RuleEngine getRuleEngine() {
        return ruleEngine;
    }

    /**
     * 获取黑方角色。
     *
     * @return 黑方角色对象
     */
    public Role getBlackRole() {
        return blackRole;
    }

    /**
     * 获取白方角色。
     *
     * @return 白方角色对象
     */
    public Role getWhiteRole() {
        return whiteRole;
    }

    /**
     * 获取当前该谁下的棋色。
     * 只可能是 Piece.BLACK 或 Piece.WHITE。
     *
     * @return 当前玩家棋色
     */
    public Piece getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * 获取当前回合数，从 1 开始。
     *
     * @return 当前回合数
     */
    public int getTurnCount() {
        return turnCount;
    }

    /**
     * 判断游戏是否已结束。
     *
     * @return 已结束返回 true
     */
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * 设置游戏结束标志。
     * 由 GameController 在判定胜负后调用。
     *
     * @param gameOver true 表示游戏结束
     */
    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    /**
     * 获取当前历史快照数量。
     * 用于判断是否还能悔棋。
     *
     * @return 快照栈中快照数量
     */
    public int getHistorySize() {
        return history.size();
    }


}
