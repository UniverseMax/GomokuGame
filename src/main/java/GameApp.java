/**
 * Gobattle 程序入口。
 * 负责组装核心对象并启动控制台版控制器。
 */
public class GameApp {

    /**
     * 程序主入口。
     *
     * @param args 命令行参数，当前未使用
     */
    public static void main(String[] args) {
        start();
    }

    /**
     * 创建棋盘、规则引擎、角色、全局状态和控制器，并启动控制台对局。
     */
    public static void start() {
        Chessboard board = new Chessboard(15, 15);
        RuleEngine ruleEngine = new RuleEngine();

        Role blackRole = RoleFactory.create(RoleFactory.WARRIOR);
        Role whiteRole = RoleFactory.create(RoleFactory.ASTROLOGER);

        GameState state = new GameState(board, ruleEngine, blackRole, whiteRole);
        GameController controller = new GameController(state);

        controller.runConsole();
    }
}