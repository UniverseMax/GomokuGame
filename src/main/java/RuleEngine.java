import java.util.ArrayList;
import java.util.List;

public class RuleEngine {
    private final int[] DX = new int[]{1,0,1,-1};
    private final int[] DY = new int[]{0,-1,-1,-1};
    private final int WIN_COUNT = 5;

    /**
     * 检索各方向上连子情况
     * @param board 棋盘情况
     * @param row   落子行
     * @param col   落子列
     * @param piece 落子颜色
     * @return  返回连子的集合
     */
    public List<List<int[]>> detectAllLines(Chessboard board, int row, int col,Piece piece){
        List<List<int[]>> result = new ArrayList<List<int[]>>();    //用于返回的集合

        for(int dir = 0;dir<4;dir++){   //dir用于遍历DX和DY
            List<int[]> line = new ArrayList<>();   //连线集合，合并到result后被卸载
            line.add(new int[]{row,col});   //先添加落子所在位置
            collectDirection(board,row,col,DX[dir],DY[dir],piece,line);     //正向检索
            collectDirection(board,row,col,-DX[dir],-DY[dir],piece,line);   //反向检索

            if (line.size() >= WIN_COUNT){   //如果有至少五枚
                result.add(line);
            }
        }

        // 后置条件检查：Fail-Fast,防御性编程
        for (int i = 0; i < result.size(); i++) {
            List<int[]> line = result.get(i);
            if (line.size() < WIN_COUNT) {  //如果有一条线不足5枚子则触发
                throw new IllegalStateException("连珠线长度异常: " + line.size());
            }
        }

        return  result;
    }

    /**
     * 按指定方向，在棋盘上指定位置开始寻找同色棋子
     * @param board 棋盘
     * @param row   位置行
     * @param col   位置列
     * @param dirX  横向量
     * @param dirY  竖向量
     * @param piece 棋子颜色
     * @param line  需要加入的集合
     * 无需返回，直接加在集合中
     */
    public void collectDirection(Chessboard board, int row, int col, int dirX, int dirY, Piece piece,List<int[]> line){
        int x = row + dirX;
        int y = col + dirY;
        while(board.isInBounds(x,y) && board.getPiece(x,y) == piece){   //不超界且是同类棋子
            line.add(new int[]{x,y});   //添加棋子所在坐标到集合中
            x = x + dirX;
            y = y + dirY;
        }
    }
}
