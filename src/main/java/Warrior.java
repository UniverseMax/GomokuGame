/**
 * 狂战士·奎恩。
 * HP 60，ATK 0。
 * 天赋“血怒”：消除时伤害 +1，自扣 1 血。
 * 主动技能“破釜沉舟”：本回合伤害翻倍，未消除则回合结束自扣 2 血。
 *
 * 设计说明：
 * 狂战士拥有全角色最高 HP（60），但血怒每次消除自扣 1 血。
 * 高 HP 不是坦度，而是供自残机制燃烧的资源池。
 */
public class Warrior extends Role{

    /** 本回合累计的伤害加成，由血怒叠加 */
    private int damageBonus;

    /** 破釜沉舟是否已激活 */
    private boolean skillActive;

    /** 本回合是否已消除，独属于血怒技能的判定参数,判定激活技能后是否消过子 */
    private boolean eliminatedThisTurn;

    /**
     * 构造狂战士。
     * HP 60，ATK 0，技能 CD 6。
     */
    public Warrior() {
        super("狂战士·奎恩", 60, 0, 6);
        this.damageBonus = 0;
        this.skillActive = false;
        this.eliminatedThisTurn = false;
    }

    /**
     * 消子时触发的技能
     *
     * 血怒：消除时伤害 +1，自扣 1 血。
     * 注意：自扣血可能导致狂战士自我死亡，这是设计的一部分。
     *
     * @param state 游戏状态机
     * @param lineCount 消除的线的数量
     */
    @Override
    public void onEliminate(GameState state, int lineCount) {
        if (lineCount <= 0) {
            throw new IllegalArgumentException("连珠线条数必须大于0");
        }
        this.damageBonus++;
        reduceHp(1);
        eliminatedThisTurn = true;
        System.out.println("[DEBUG] 血怒触发，damageBonus=" + damageBonus + "，自扣后 HP=" + getHp());
    }

    /**
     * 主动技能
     *
     * 破釜沉舟：本回合伤害翻倍。
     * 若本回合未消除，回合结束时自扣 2 血。
     *
     * 翻两倍的代码在本类的getDamageMultiplier()中实现
     *
     * @param state 游戏状态机
     */
    @Override
    public void castSkill(GameState state) {
        if (!isSkillReady()) {     //
            throw new IllegalStateException("破釜沉舟尚未冷却完毕");
        }
        skillActive = true;     //技能激活
        this.eliminatedThisTurn = false;
        startCooldown();    //重置CD
        System.out.println("[DEBUG] 破釜沉舟激活，CD=" + getSkillCooldown());
    }

    /**
     * 获取本回合伤害加成。
     *
     * @return 血怒累计的伤害加成
     */
    @Override
    public int getDamageBonus() {
        return this.damageBonus;
    }

    /**
     * 获取本回合伤害倍率。
     *
     * @return 破釜沉舟激活时为 2.0，否则为 1.0
     */
    @Override
    public double getDamageMultiplier() {
        if (this.skillActive) return 2.0;
        System.out.println("[DEBUG] getDamageMultiplier 被读，skillActive=" + skillActive);
        return 1.0;
    }

    /**
     * 回合结束：
     * 1. 若破釜沉舟已激活且本回合未消除，自扣 2 血。
     * 2. 重置本回合所有临时状态。
     *
     * @param state 游戏状态
     */
    @Override
    public void onTurnEnd(GameState state) {
        System.out.println("[DEBUG] 奎恩回合结束，skillActive=" + skillActive + "，eliminatedThisTurn=" + eliminatedThisTurn);
        if (this.skillActive && !this.eliminatedThisTurn) {
            this.reduceHp(2);
        }
        this.skillActive = false;
        this.damageBonus = 0;
        this.eliminatedThisTurn = false;
    }
}
