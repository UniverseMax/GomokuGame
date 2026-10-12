import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Chessboard {
    private final Piece[][] board;    //棋盘数组


    /**
     * 无参数型构造方法，传入默认棋盘大小15x15
     */
    public Chessboard() {
        this(15,15);
    }

    /**
     * 单参数构造方法，按正方形棋盘传入边界大小
     * @param size  边界长度
     */
    public Chessboard(int size){
        this(size,size);
    }

    /**
     * 双参数构造方法，按行列定义棋盘大小
     * @param row   行数
     * @param col   列数
     */
    public Chessboard(int row, int col) {
        board = new Piece[row][col];
        reset();
    }

    /**
     * 初始化棋盘
     */
    public void reset(){
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                board[i][j] = Piece.EMPTY;      //全部填为空值
            }
        }
    }

    /**
     * 落下一枚棋子
     * @param row   落子的行数
     * @param col   落子的列数
     * @param piece 落子的颜色（或者说棋子对象）
     * @return 落子成功返回true
     */
    public boolean place(int row, int col, Piece piece){
        if(!isInBounds(row,col)) return false;
        if (piece == null || piece == Piece.EMPTY) return false;    //不允许传入null和空白棋子
        if(!isEmpty(row, col)) return false; //如果落子处不为空，则返回false
        board[row][col] = piece;
        return true;
    }

    /**
     * 消除单颗棋子
     * @param row   所要消除棋子的行数
     * @param col   所要消除棋子的列数
     * @return 消子成功返回true
     */
    public boolean remove(int row, int col){
        if(!isInBounds(row,col)) return false;
        if(isEmpty(row, col)) return false;    //如果落子处为空，则返回false
        board[row][col] = Piece.EMPTY;
        return true;
    }

    /**
     * 连续消子，内部自查重复
     * @param positions 需要连续消子的棋子位置列表
     * @return 返回消除了几枚子，返回非正整数即为报错
     */
    public int removeAll(List<int[]> positions){
        if(positions == null || positions.isEmpty()) return 0;    //非空值校验

        Set<String> removed = new HashSet<>();  //用于去重的容器
        int count = 0;

        for (int[] position : positions) {    //如果校验失败则直接进入下一轮循环，跳过对其的操作
            // 坐标合法性检查,如null，元素数不等于2
            if (position == null || position.length != 2) continue;

            int row = position[0];
            int col = position[1];

            //边界检查
            if (!isInBounds(row, col)) continue;

            //空位检查
            if (board[row][col] == Piece.EMPTY) continue;

            //去重检查
            String key = row + "," + col;
            if (!removed.add(key)) continue;

            //实际消除操作
            board[row][col] = Piece.EMPTY;  //不用remove方法是因为已经进行过检查了，不用重复操作

            //消子计数
            count++;
        }

        return count;
    }

    /**
     * 从快照恢复棋盘
     * @param pieces    棋盘快照
     * @return  恢复成功返回true
     */
    public boolean restore(Piece[][] pieces){
        if(pieces == null || pieces.length != board.length) return false;  //如果快照不合规则返回false

        for (int i = 0; i < pieces.length; i++) {
            if(pieces[i] == null || pieces[i].length != board[i].length) return false;  //检查每一行是否含有错误
            for (int j = 0; j < pieces[i].length; j++) {
                board[i][j] = pieces[i][j];
            }
        }
        return true;
    }

    /**
     * 超界检查
     * @param row   棋子的行数
     * @param col   棋子的列数
     * @return  没有超界返回true，否则返回false
     */
    public boolean isInBounds(int row, int col){
        return row >= 0 && row < board.length && col >= 0 && col < board[row].length;
    }


    /**
     * 判断位置是否为空
     * @param row   行数
     * @param col   列数
     * @return  若为空返回true
     */
    public boolean isEmpty(int row, int col){
        if (!isInBounds(row, col)) return false;    //越界检查
        return board[row][col] == Piece.EMPTY;
    }

    /**
     * 检查棋盘是否已满，作为平局依据
     * @return  棋盘若没有空位，返回true
     */
    public boolean isFull(){
        for (Piece[] pieces : board) {
            for (Piece piece : pieces) {
                if (piece == Piece.EMPTY) return false;
            }
        }
        return true;
    }

    /**
     * 深度复制棋盘作为快照
     * @return  返回Piece数组作为棋盘快照
     */
    public Piece[][] snapshot(){
        Piece[][] copy = new Piece[board.length][board[0].length];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                copy[i][j] = board[i][j];
            }
        }
        return copy;
    }

    /**
     * 调试用，显示棋盘到输出
     * @return  返回表示棋盘当前状态的字符串
     */
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append("   ");
        for (int i = 0; i < board.length; i++) {
            sb.append(String.format("%2d ",i));
        }
        sb.append("\n");
        for (int i = 0; i < board.length; i++) {
            sb.append(String.format("%2d ",i));
            for (int j = 0; j < board[i].length; j++) {
                switch (board[i][j]) {
                    case BLACK: sb.append(" X "); break;
                    case WHITE: sb.append(" O "); break;
                    default:    sb.append(" . "); break;
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * 返回棋盘边长的数组
     * @return  含有棋盘边长的数组
     */
    public int[] getSize(){
        return new int[]{board.length,board[0].length};
    }

    //将坐标拆分转换为int型
    public static int getCoordinateByIndex(String str, int index) {
        if (str == null || str.isEmpty() || index < 0) {
            return -1;
        }
        String[] parts = str.split(",");
        if (index >= parts.length) {
            return -1;
        }
        try {
            return Integer.parseInt(parts[index].trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * 按位置返回棋子
     * @param row   棋子行数
     * @param col   棋子列数
     * @return  返回棋子，超界返回空
     */
    public Piece getPiece(int row, int col){
        if(!isInBounds(row,col)) return Piece.EMPTY;
        return board[row][col];
    }

}
