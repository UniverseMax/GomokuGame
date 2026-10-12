/**
 * 圣骑士·罗兰。
 * <p>
 * HP 40，ATK 0，技能 CD 6。
 * <p>
 * 天赋“圣盾壁垒”：受伤时获得 {@code ceil(伤害 × 20%)} 点护盾，
 * 同时累积伤害减免层数，满 3 层时下一次伤害完全免疫并清空层数。
 * <p>
 * 主动技能“神圣审判”：本回合若消除，伤害 +2；若未消除，回合结束回 2 血。
 * <p>
 * 反制机制：对手单次消除棋子数 ≥ 6 时，层数立即归零，本次伤害减半，
 * 且本次不再累积层数。
 */
public class Paladin extends Role {
    /** 伤害减免层数，上限 3。满 3 层时下次伤害免疫 */
    private int reductionLayers;

    /** 护盾数值池，用于吸收伤害，无上限 */
    private int shield;

    /** 神圣审判是否已激活 */
    private boolean skillActive;

    /** 本回合是否已消除 */
    private boolean eliminatedThisTurn;

    /**
     * 构造圣骑士。
     * HP 40，ATK 0，技能 CD 6。初始护盾 0，层数 0。
     */
    public Paladin() {
        super("圣骑士·罗兰", 40, 0, 6);
        this.reductionLayers=0;
        this.skillActive=false;
        this.shield=0;
        this.eliminatedThisTurn=false;
    }

    /**
     * 受伤结算。流程：
     * <ol>
     *     <li>若为大招（6 子及以上）：层数归零，伤害减半</li>
     *     <li>否则若层数满 3：免疫本次伤害，层数归零</li>
     *     <li>先扣护盾（护盾不享受减伤）</li>
     *     <li>溢出伤害扣血</li>
     *     <li>按减伤后的伤害获得新护盾</li>
     *     <li>非大招且本次有实际伤害时，层数 +1（上限 3）</li>
     * </ol>
     *
     * @param damage      受到的伤害，不能为负数
     * @param isBigAttack 对手本次消除棋子数是否达到 6 子及以上
     * @throws IllegalArgumentException damage 为负数时抛出
     */
    @Override
    public void reduceHp(int damage, boolean isBigAttack) {
        int overflow;   //溢出伤害，用于计算护盾破碎后溢出的伤害
        if (damage < 0) {
            throw new IllegalArgumentException("伤害值不可小于0");
        }
        else if (damage == 0) {
            return;
        }
        if (isBigAttack){
            reductionLayers = 0;
            damage = damage / 2;
            System.out.println("[DEBUG] 圣骑士遭大招，层数归零，伤害减半为 " + damage);
        }
        if (reductionLayers >= 3) {
            reductionLayers = 0;
            damage = 0;
            System.out.println("[DEBUG] 圣骑士 3 层免疫触发，层数归零");
        }
        if (shield >= damage){
            shield = shield - damage;
            overflow = 0;
        }
        else {
            overflow = damage - shield;
            shield = 0;
        }
        super.reduceHp(overflow, false);
        shield += (int) Math.ceil(damage * 0.2);
        if (!isBigAttack && damage > 0) {
            reductionLayers++;
            if (reductionLayers > 3) {
                reductionLayers = 3;
            }
        }
        System.out.println("[DEBUG] 圣骑士结算后 shield=" + shield + "，layers=" + reductionLayers + "，hp=" + getHp());
    }

    /**
     * 天赋触发：仅标记本回合已消除，供神圣审判结算使用。
     * 圣盾壁垒的护盾逻辑在 {@link #reduceHp} 中处理，不在此处。
     *
     * @param state     游戏状态机
     * @param lineCount 本次消除的连珠线条数，必须大于 0
     * @throws IllegalArgumentException lineCount 非正时抛出
     */
    @Override
    public void onEliminate(GameState state, int lineCount) {
        if (lineCount <= 0) {
            throw new IllegalArgumentException("连珠线条数必须大于0");
        }
        this.eliminatedThisTurn=true;
    }

    /**
     * 释放神圣审判：激活技能，重置本回合消除标记，进入冷却。
     *
     * @param state 游戏状态机
     * @throws IllegalStateException 技能尚未冷却完毕时抛出
     */
    @Override
    public void castSkill(GameState state) {
        if (!isSkillReady()) {
            throw new IllegalStateException("大招未冷却完毕");
        }
        this.skillActive=true;
        this.eliminatedThisTurn=false;
        startCooldown();
    }

    /**
     * 获取本回合伤害加成。
     * 神圣审判激活且本回合已消除时返回 2，否则返回 0。
     *
     * @return 0 或 2
     */
    @Override
    public int getDamageBonus() {
        if (skillActive && eliminatedThisTurn) {
            return 2;
        }
        else {
            return 0;
        }
    }

    /**
     * 回合结束：
     * 1. 若神圣审判已激活且本回合未消除，回 2 血。
     * 2. 重置技能状态。
     * 注意：护盾和伤害减免层数不重置，它们是跨回合资源。
     *
     * @param state 游戏状态机
     */
    @Override
    public void onTurnEnd(GameState state) {
        if (skillActive && !eliminatedThisTurn) {
            System.out.println("[DEBUG] 神圣审判未消除，回 2 血，当前 HP=" + getHp());
            heal(2);
        }
        this.skillActive=false;
        this.eliminatedThisTurn=false;
    }

    /**
     * 获取当前护盾值，供 UI 显示。
     *
     * @return 当前护盾
     */
    public int getShield() {
        return shield;
    }

    /**
     * 获取当前伤害减免层数，供 UI 显示。
     *
     * @return 当前层数，0-3
     */
    public int getReductionLayers() {
        return reductionLayers;
    }
}
