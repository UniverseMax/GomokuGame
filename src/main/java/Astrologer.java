import java.util.*;

/**
 * 占星师·艾琳。
 * <p>
 * HP 40，ATK 1，技能 CD 6。
 * <p>
 * 天赋“预兆之眼”：占星师每次落子后，在落子位置上、下、左、右 4 格生成预标记。
 * 预标记不显示，仅后台记录。占星师下次落子时清空旧预标记并重新生成。
 * 若对手落在预标记上，该位置转为真标记，其他预标记全部清除。
 * 真标记对所有人显示，存在 6 回合（占星师 3 次回合结束）后消失。
 * <p>
 * 主动技能“星辰坍塌”：选择并清除一个真标记，消除该位置的棋子，
 * 并从该位置上、下、左、右 4 个方向各 2 格范围内随机消除至多 2 颗棋子（任意方）。
 * 大招消除的棋子不计入连珠消除，不触发伤害公式。
 */
public class Astrologer extends Role {

    /** 真标记的存活检查次数。占星师每回合结束减 1，减到 0 移除。3 次检查 = 6 个总回合 */
    private static final int TRUE_MARK_CHECKS = 3;

    /** 预标记集合，元素格式 "row,col"。占星师每次落子重建 */
    private Set<String> preMarks;

    /** 真标记集合，key 为 "row,col"，value 为剩余检查次数 */
    private Map<String, Integer> trueMarks;

    /**
     * 构造占星师。
     * HP 40，ATK 1，技能 CD 6。
     */
    public Astrologer() {
        super("占星师·艾琳", 40, 1, 6);
        preMarks = new HashSet<>();
        trueMarks = new HashMap<>();
    }

    /**
     * 占星师的天赋不在消除时触发，此方法为空实现。
     *
     * @param state     游戏状态机
     * @param lineCount 本次消除的连珠线条数
     */
    @Override
    public void onEliminate(GameState state, int lineCount) {
        // 天赋在 onSelfPlace 中触发，此处无操作
    }

    /**
     * 释放星辰坍塌。
     * 流程：
     * <ol>
     *     <li>检查技能 CD</li>
     *     <li>从 GameState 读取玩家选中的真标记</li>
     *     <li>校验目标是否为有效真标记</li>
     *     <li>解析目标坐标</li>
     *     <li>从真标记集合移除该目标</li>
     *     <li>消除中心棋子</li>
     *     <li>收集上下左右各 2 格的候选位置</li>
     *     <li>随机选至多 2 个消除</li>
     *     <li>进入冷却</li>
     * </ol>
     *
     * @param state 游戏状态机
     * @throws IllegalStateException 技能未冷却、未选目标、目标非法时抛出
     */
    @Override
    public void castSkill(GameState state) {
        // 1. 检查 CD
        if (!isSkillReady()) {
            throw new IllegalStateException("星辰坍塌尚未冷却完毕");
        }

        // 2. 读目标
        String target = state.getSelectedMark();
        if (target == null) {
            throw new IllegalStateException("未选择引爆目标");
        }
        if (!trueMarks.containsKey(target)) {
            throw new IllegalStateException("目标不是有效的真标记: " + target);
        }

        // 3. 解析坐标
        int r = Chessboard.getCoordinateByIndex(target, 0);
        int c = Chessboard.getCoordinateByIndex(target, 1);
        if (r < 0 || c < 0) {
            throw new IllegalStateException("目标坐标非法: " + target);
        }

        // 4. 消除中心棋子
        Chessboard board = state.getBoard();
        if (board.isEmpty(r, c)) {
            throw new IllegalStateException("真标记位置没有棋子可消除: " + target);
        }
        System.out.println("[DEBUG] 星辰坍塌目标: " + target + "，中心有子=" + !board.isEmpty(r, c));

        // 5. 清除真标记
        board.remove(r, c);
        trueMarks.remove(target);
        state.clearSelectedMark();

        // 6. 收集候选：上下左右各 2 格
        List<int[]> candidates = new ArrayList<>();
        int[][] DIRECTIONS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] dir : DIRECTIONS) {
            for (int step = 1; step <= 2; step++) {
                int nr = r + dir[0] * step;
                int nc = c + dir[1] * step;
                if (!board.isInBounds(nr, nc)) continue;
                if (board.isEmpty(nr, nc)) continue;
                candidates.add(new int[]{nr, nc});
            }
        }
        System.out.println("[DEBUG] 候选格子数: " + candidates.size() + "，实际消除: " + Math.min(2, candidates.size()));

        // 7. 随机选至多 2 个
        Collections.shuffle(candidates);
        int count = Math.min(2, candidates.size());
        for (int i = 0; i < count; i++) {
            int[] pos = candidates.get(i);
            board.remove(pos[0], pos[1]);
        }

        // 8. 进入冷却
        startCooldown();
    }

    /**
     * 对手落子成功后触发。
     * 若落子位置在预标记中，转为真标记并清空所有预标记。
     *
     * @param state 游戏状态机
     * @param row   对手落子行
     * @param col   对手落子列
     */
    @Override
    public void onOpponentPlace(GameState state, int row, int col) {
        String key = row + "," + col;
        if (preMarks.contains(key)) {
            trueMarks.put(key, TRUE_MARK_CHECKS);
            preMarks.clear();
            System.out.println("[DEBUG] 对手落在预标记上 " + key + "，转为真标记");
        }
    }

    /**
     * 自己落子成功后触发。
     * 清空旧预标记，在落子位置上、下、左、右 4 格生成新预标记。
     * 越界位置跳过，非空位置也标记。
     *
     * @param state 游戏状态机
     * @param row   落子行
     * @param col   落子列
     */
    @Override
    public void onSelfPlace(GameState state, int row, int col) {
        preMarks.clear();
        int[] dx = {-1, 0, 0, 1};
        int[] dy = {0, 1, -1, 0};
        for (int i = 0; i < 4; i++) {
            int nr = row + dx[i];
            int nc = col + dy[i];
            if (!state.getBoard().isInBounds(nr, nc)) {
                continue;
            }
            preMarks.add(nr + "," + nc);
        }
        System.out.println("[DEBUG] 艾琳落子 (" + row + "," + col + ") 生成预标记: " + preMarks);
    }

    /**
     * 回合结束：所有真标记剩余次数减 1，减到 0 的移除。
     * 使用 Iterator 遍历，避免 ConcurrentModificationException。
     *
     * @param state 游戏状态机
     */
    @Override
    public void onTurnEnd(GameState state) {
        Iterator<Map.Entry<String, Integer>> it = trueMarks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> entry = it.next();
            int remaining = entry.getValue() - 1;
            if (remaining <= 0) {
                it.remove();
            } else {
                entry.setValue(remaining);
            }
        }
        System.out.println("[DEBUG] 回合结束，真标记剩余: " + trueMarks);
    }

    /**
     * 获取当前所有真标记的副本，供 UI 显示。
     * 返回的是深拷贝，调用方修改不会影响内部状态。
     *
     * @return 真标记 Map，key 为 "row,col"，value 为剩余检查次数
     */
    public Map<String, Integer> getTrueMarks() {
        return new HashMap<>(trueMarks);
    }
}