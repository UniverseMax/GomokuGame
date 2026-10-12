import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * 游戏总控制器（控制台版）。
 * 负责调度：落子 → 检测 → 消除 → 扣血 → 回合收尾 → 切换 → 判胜负。
 * 已接入控制台技能指令：skill / mark / help。
 */
public class GameController {

    private final GameState state;

    /**
     * 构造控制器。
     *
     * @param state 游戏状态，不能为 null
     * @throws IllegalArgumentException state 为 null 时抛出
     */
    public GameController(GameState state) {
        if (state == null) {
            throw new IllegalArgumentException("游戏状态不能为null");
        }
        this.state = state;
    }

    /**
     * 启动控制台游戏主循环。
     * 支持指令：row col 落子、skill 释放技能、mark 查看/选择真标记、help 帮助、q 退出。
     */
    public void runConsole() {
        Scanner scanner = new Scanner(System.in);
        printWelcome();

        while (!state.isGameOver()) {
            printBoard();
            printStatus();

            System.out.print("请输入指令（落子: row col，help 查看帮助，q 退出）: ");
            if (!scanner.hasNextLine()) {
                break;
            }
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            String lower = input.toLowerCase();

            if (lower.equals("q") || lower.equals("quit") || lower.equals("exit")) {
                System.out.println("已退出游戏。");
                return;
            }

            if (lower.equals("help")) {
                printHelp();
                continue;
            }

            if (lower.equals("mark") || lower.startsWith("mark ")) {
                handleMarkCommand(input);
                continue;
            }

            if (lower.equals("skill") || lower.startsWith("skill ")) {
                handleSkillCommand(input);
                continue;
            }

            int[] pos = parsePosition(input);
            if (pos == null) {
                System.out.println("输入格式错误。落子请输入两个整数（如: 7 7），或输入 help 查看帮助。");
                continue;
            }

            boolean success = playMove(pos[0], pos[1]);
            if (!success) {
                System.out.println("落子失败：位置越界、已被占用或游戏已结束。");
            }
        }

        printBoard();
        printFinalResult();
    }

    /**
     * 执行一次落子及其后续结算。
     *
     * @param row 落子行
     * @param col 落子列
     * @return 落子并结算成功返回 true；位置非法返回 false
     */
    public boolean playMove(int row, int col) {
        if (state.isGameOver()) {
            return false;
        }

        Piece current = state.getCurrentPlayer();
        if (!state.getBoard().place(row, col, current)) {
            return false;
        }

        state.getCurrentRole().onSelfPlace(state, row, col);
        state.getOpponentRole().onOpponentPlace(state, row, col);
        state.saveSnapshot();

        List<List<int[]>> lines = state.getRuleEngine()
                .detectAllLines(state.getBoard(), row, col, current);

        if (!lines.isEmpty()) {
            resolveElimination(lines);
            if (state.isGameOver()) {
                return true;
            }
        }

        endTurn();
        return true;
    }

    /**
     * 结算连珠消除与伤害。
     *
     * @param lines 连珠线集合，不能为空
     */
    private void resolveElimination(List<List<int[]>> lines) {
        Role attacker = state.getCurrentRole();
        Role defender = state.getOpponentRole();

        attacker.onEliminate(state, lines.size());

        int eliminatedCount = countUniquePieces(lines);

        int base = eliminatedCount;
        int extra = eliminatedCount > 5
                ? (eliminatedCount - 5) * (eliminatedCount - 5)
                : 0;
        int flat = base + extra + attacker.getAtk() + attacker.getDamageBonus();
        int damage = (int) Math.floor(flat * attacker.getDamageMultiplier());

        System.out.println("[DEBUG] 伤害构成: base=" + base + " extra=" + extra
                + " atk=" + attacker.getAtk()
                + " bonus=" + attacker.getDamageBonus()
                + " multiplier=" + attacker.getDamageMultiplier()
                + " → damage=" + damage);

        System.out.printf("%s 形成 %d 条连珠，消除 %d 子，造成 %d 点伤害！%n",
                attacker.getName(), lines.size(), eliminatedCount, damage);

        boolean isBigAttack = eliminatedCount >= 6;

        defender.reduceHp(damage, isBigAttack);
        System.out.printf("%s 剩余 HP: %d/%d%n",
                defender.getName(), defender.getHp(), defender.getMaxHp());

        for (List<int[]> line : lines) {
            state.getBoard().removeAll(line);
        }

        if (defender.isDead()) {
            state.setGameOver(true);
            System.out.printf("%s 被击败！%n", defender.getName());
        }
    }

    /**
     * 统计连珠线中实际不重复的棋子数量。
     * 交叉线可能共享同一颗子，必须去重。
     *
     * @param lines 连珠线集合
     * @return 去重后的棋子数
     */
    private int countUniquePieces(List<List<int[]>> lines) {
        Set<String> unique = new HashSet<>();
        for (List<int[]> line : lines) {
            for (int[] pos : line) {
                unique.add(pos[0] + "," + pos[1]);
            }
        }
        return unique.size();
    }

    /**
     * 回合收尾：角色结算、CD 回转、切换玩家。
     */
    private void endTurn() {
        Role currentRole = state.getCurrentRole();
        currentRole.onTurnEnd(state);
        currentRole.tickCooldown();
        state.switchPlayer();
        System.out.println("[DEBUG] 回合切换，turnCount=" + state.getTurnCount()
                + "，下一手=" + state.getCurrentPlayer());
    }

    // ==================== 技能指令处理 ====================

    /**
     * 处理 skill 指令。
     * 技能不消耗回合，玩家可在同一回合内先放技能再落子。
     * 占星师必须先选择真标记（用 mark 或 skill row,col）。
     *
     * @param input 原始输入，形如 "skill" / "skill 7,7" / "skill 7 7"
     */
    private void handleSkillCommand(String input) {
        Role current = state.getCurrentRole();

        if (!current.isSkillReady()) {
            System.out.printf("%s 的技能冷却中：%d/%d%n",
                    current.getName(),
                    current.getSkillCooldown(),
                    current.getMaxSkillCooldown());
            return;
        }

        String[] parts = input.trim().split("\\s+");

        String targetStr = null;
        if (parts.length == 2) {
            targetStr = parts[1];                       // skill 7,7
        } else if (parts.length == 3) {
            targetStr = parts[1] + " " + parts[2];      // skill 7 7
        } else if (parts.length > 3) {
            System.out.println("用法：skill 或 skill row,col 或 skill row col");
            return;
        }

        if (targetStr != null) {
            if (!(current instanceof Astrologer)) {
                System.out.println(current.getName() + " 不支持指定目标坐标。");
                return;
            }
            int[] pos = parsePosition(targetStr);
            if (pos == null) {
                System.out.println("坐标格式错误，应为 row,col 或 row col。");
                return;
            }
            state.setSelectedMark(pos[0] + "," + pos[1]);
        }

        if (current instanceof Astrologer && state.getSelectedMark() == null) {
            System.out.println("占星师释放技能前必须选择真标记。");
            System.out.println("用法：mark row,col 选择目标，或 skill row,col 一步完成。");
            return;
        }

        // 关键修复：castSkill 之前先保存标记。
        // Astrologer.castSkill 内部会调用 state.clearSelectedMark()，
        // 之后再读 state.getSelectedMark() 会得到 null。
        String markBeforeCast = state.getSelectedMark();

        current.castSkill(state);
        System.out.println(current.getName() + " 释放了技能！");

        if (current instanceof Astrologer) {
            System.out.println("  已引爆真标记：" + markBeforeCast);
            // 无需再调 state.clearSelectedMark()：
            // Astrologer.castSkill 内部已完成清理，外部重复调用只是冗余。
        }

        System.out.println("  技能不消耗回合，本回合仍可落子。");
    }

    /**
     * 处理 mark 指令，仅占星师有效。
     * 无参数：列出所有真标记及其剩余检查次数。
     * 带坐标：选中该坐标作为技能目标。
     *
     * @param input 原始输入，形如 "mark" / "mark 7,7" / "mark 7 7"
     */
    private void handleMarkCommand(String input) {
        Role current = state.getCurrentRole();

        if (!(current instanceof Astrologer)) {
            System.out.println("当前角色 " + current.getName() + " 不是占星师，没有真标记。");
            return;
        }

        Astrologer astrologer = (Astrologer) current;
        Map<String, Integer> marks = astrologer.getTrueMarks();

        String[] parts = input.trim().split("\\s+");

        if (parts.length == 1) {
            if (marks.isEmpty()) {
                System.out.println("当前没有真标记。");
                return;
            }
            System.out.println("当前真标记（剩余检查次数）：");
            for (Map.Entry<String, Integer> entry : marks.entrySet()) {
                String key = entry.getKey();
                String flag = key.equals(state.getSelectedMark()) ? "  ← 已选中" : "";
                System.out.printf("  %s  剩余 %d 次%s%n",
                        key, entry.getValue(), flag);
            }
            return;
        }

        String targetStr;
        if (parts.length == 2) {
            targetStr = parts[1];
        } else if (parts.length == 3) {
            targetStr = parts[1] + " " + parts[2];
        } else {
            System.out.println("用法：mark 或 mark row,col 或 mark row col");
            return;
        }

        int[] pos = parsePosition(targetStr);
        if (pos == null) {
            System.out.println("坐标格式错误，应为 row,col 或 row col。");
            return;
        }

        String key = pos[0] + "," + pos[1];
        if (!marks.containsKey(key)) {
            System.out.println("位置 " + key + " 不是真标记。");
            return;
        }

        state.setSelectedMark(key);
        System.out.println("已选中真标记：" + key
                + "（剩余检查次数 " + marks.get(key) + "）");
    }

    /**
     * 打印指令帮助。
     */
    private void printHelp() {
        System.out.println("===== 指令列表 =====");
        System.out.println("  row col        落子，如 7 7");
        System.out.println("  row,col        同上（逗号分隔）");
        System.out.println("  skill          释放当前角色技能（不消耗回合）");
        System.out.println("  skill row,col  占星师：选择真标记并释放技能");
        System.out.println("  mark           占星师：列出所有真标记");
        System.out.println("  mark row,col   占星师：选择目标真标记");
        System.out.println("  help           显示本帮助");
        System.out.println("  q              退出游戏");
        System.out.println();
    }

    // ==================== 工具与打印 ====================

    /**
     * 解析坐标输入，同时支持 "7 7" 和 "7,7"。
     *
     * @param input 坐标字符串
     * @return 长度为 2 的数组；格式错误返回 null
     */
    private int[] parsePosition(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        String normalized = input.replace(',', ' ').trim();
        String[] parts = normalized.split("\\s+");
        if (parts.length != 2) {
            return null;
        }
        try {
            int row = Integer.parseInt(parts[0]);
            int col = Integer.parseInt(parts[1]);
            return new int[]{row, col};
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 打印欢迎信息。
     */
    private void printWelcome() {
        System.out.println("===== Gobattle 五子棋对战 =====");
        System.out.println("黑方: " + state.getBlackRole().getName());
        System.out.println("白方: " + state.getWhiteRole().getName());
        System.out.println("落子: row col（如 7 7）  帮助: help  退出: q");
        System.out.println("技能: skill（占星师先 mark row,col 选真标记）");
        System.out.println();
    }

    /**
     * 打印棋盘。
     */
    private void printBoard() {
        System.out.print(state.getBoard());
    }

    /**
     * 打印当前回合、双方 HP、技能 CD 与已选真标记。
     */
    private void printStatus() {
        System.out.printf("第 %d 回合 | 当前: %s (%s) %n",
                state.getTurnCount(),
                state.getCurrentPlayer(),
                state.getCurrentRole().getName());
        System.out.printf("  黑方 %s HP:%d/%d%n",
                state.getBlackRole().getName(),
                state.getBlackRole().getHp(),
                state.getBlackRole().getMaxHp());
        System.out.printf("  白方 %s HP:%d/%d%n",
                state.getWhiteRole().getName(),
                state.getWhiteRole().getHp(),
                state.getWhiteRole().getMaxHp());

        Role black = state.getBlackRole();
        Role white = state.getWhiteRole();
        System.out.printf("  黑方 %s 技能: %s（CD %d/%d）%n",
                black.getName(),
                black.isSkillReady() ? "就绪" : "冷却中",
                black.getSkillCooldown(),
                black.getMaxSkillCooldown());
        System.out.printf("  白方 %s 技能: %s（CD %d/%d）%n",
                white.getName(),
                white.isSkillReady() ? "就绪" : "冷却中",
                white.getSkillCooldown(),
                white.getMaxSkillCooldown());

        if (state.getSelectedMark() != null) {
            System.out.println("  已选中真标记: " + state.getSelectedMark());
        }

        System.out.println();
    }

    /**
     * 打印最终结果。
     */
    private void printFinalResult() {
        System.out.println();
        System.out.println("===== 游戏结束 =====");
        Role black = state.getBlackRole();
        Role white = state.getWhiteRole();

        if (black.isDead() && white.isDead()) {
            System.out.println("双方同归于尽，平局。");
        } else if (black.isDead()) {
            System.out.println("白方 " + white.getName() + " 获胜！");
        } else if (white.isDead()) {
            System.out.println("黑方 " + black.getName() + " 获胜！");
        } else {
            System.out.println("游戏中止。");
        }
    }
}