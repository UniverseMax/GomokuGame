public abstract class Role {
    protected final String name;
    protected int hp;
    protected final int maxHp;
    protected final int atk;
    protected int skillCooldown;    //CD
    protected final int maxSkillCooldown;   //最大CD

    public Role(String name, int maxHp, int atk, int maxSkillCooldown) {

        //防御性编程，不静默处理
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("角色名不能为空");
        }
        if (maxHp <= 0) {
            throw new IllegalArgumentException("最大生命值必须大于0");
        }
        if (atk < 0) {
            throw new IllegalArgumentException("攻击力不能为负数");
        }
        if (maxSkillCooldown < 0) {
            throw new IllegalArgumentException("技能冷却不能为负数");
        }

        this.name = name;
        this.maxHp = maxHp;
        this.atk = atk;
        this.maxSkillCooldown = maxSkillCooldown;

        this.hp = maxHp;
        this.skillCooldown = 0;
    }

    /**
     * 消子时触发的技能方法
     * @param state 游戏状态机
     * @param lineCount 消子数量
     */
    public abstract void onEliminate(GameState state, int lineCount);

    /**
     * 主动技能方法
     * @param state 游戏状态机
     */
    public abstract void castSkill(GameState state);

    /**
     * 受伤方法（单参数重载）。
     * 默认视为非大招伤害，委托给双参数版本。
     *
     * @param damage 受到的伤害
     */
    public void reduceHp(int damage) {
        reduceHp(damage, false);
    }

    /**
     * 受伤方法（双参数版本）。
     * 默认实现直接扣血，且 HP 不会低于 0。
     * 子类可重写以处理护盾、免疫、减伤等机制。
     *
     * @param damage      受到的伤害，不能为负数
     * @param isBigAttack 对手本次消除棋子数是否达到 6 子及以上
     * @throws IllegalArgumentException damage 为负数时抛出
     */
    public void reduceHp(int damage, boolean isBigAttack) {
        if (damage < 0) {
            throw new IllegalArgumentException("伤害值不能为负数");
        }
        hp = hp - damage;
        if (hp <= 0) {
            hp = 0;
        }
    }

    /**
     * 对手落子成功后触发。
     * 默认无操作。
     */
    public void onOpponentPlace(GameState state, int row, int col) {
    }

    /**
     * 回血，不超过 maxHp
     * @param amount    回复的血量
     */
    public void heal(int amount){
        if (amount < 0) {
            throw new IllegalArgumentException("治疗量不能为负数");
        }
        hp = hp + amount;
        if (hp >= maxHp) {
            hp = maxHp;
        }
    }

    /**
     * 死亡判断
     * @return  hp为0时返回true
     */
    public boolean isDead(){
        return hp <= 0;
    }

    /**
     * CD回转，每回合减少1,不少于0
     */
    public void tickCooldown(){
        skillCooldown = skillCooldown - 1;
        if (skillCooldown <= 0) {
            skillCooldown = 0;
        }
    }

    /**
     * 判断技能是否可用
     * @return  可用时返回true
     */
    public boolean isSkillReady(){
        return skillCooldown == 0;
    }

    /**
     * 技能释放后重置CD
     */
    protected void startCooldown(){
        skillCooldown = maxSkillCooldown;
    }

    /**
     * 回合结束时触发。
     * 由 GameController 在切换玩家前调用。
     * 子类可重写，用于处理“本回合未消除则自扣血”这类延时效果。
     * 默认无操作。
     *
     * @param state 游戏状态
     */
    public void onTurnEnd(GameState state) {
        // 默认无操作
    }

    /**
     * 自己落子成功后触发。
     * 由 GameController 在落子成功后对当前角色调用。
     * 默认无操作，子类可重写以处理落子后的效果。
     *
     * @param state 游戏状态机
     * @param row   落子行
     * @param col   落子列
     */
    public void onSelfPlace(GameState state, int row, int col) {
        // 默认无操作
    }

    /**
     * 获取角色名。
     *
     * @return 角色名字符串
     */
    public String getName() {
        return name;
    }

    /**
     * 获取当前生命值。
     *
     * @return 当前 HP
     */
    public int getHp() {
        return hp;
    }

    /**
     * 获取最大生命值。
     *
     * @return 最大 HP
     */
    public int getMaxHp() {
        return maxHp;
    }

    /**
     * 获取攻击力。
     * 攻击力用于伤害公式：伤害 = 连珠线条数 × (1 + atk)
     *
     * @return 攻击力数值
     */
    public int getAtk() {
        return atk;
    }

    /**
     * 获取当前技能冷却回合数。
     * 0 表示技能可用。
     *
     * @return 当前技能 CD
     */
    public int getSkillCooldown() {
        return skillCooldown;
    }

    /**
     * 获取技能最大冷却回合数。
     * 技能释放后，CD 会重置为该值。
     *
     * @return 技能最大 CD
     */
    public int getMaxSkillCooldown() {
        return maxSkillCooldown;
    }

    /**
     * 获取本回合临时伤害加成。
     * 默认返回 0，子类可重写以提供天赋/技能带来的伤害修正。
     * 该值由子类自行在回合结束时重置。
     *
     * @return 伤害加成值，不能为负数
     */
    public int getDamageBonus() {
        return 0;
    }

    /**
     * 获取本回合伤害倍率。
     * 默认返回 1.0，子类可重写以提供伤害翻倍等效果。
     * 最终伤害 = (基础伤害 + 额外伤害 + ATK + getDamageBonus()) × getDamageMultiplier()
     *
     * @return 伤害倍率，必须大于 0
     */
    public double getDamageMultiplier() {
        return 1.0;
    }

    /**
     * 判断角色是否死亡。
     * 重写 Object.toString()，便于调试与日志输出。
     *
     * @return 返回角色状态摘要字符串
     */
    @Override
    public String toString() {
        return String.format("%s HP:%d/%d ATK:%d CD:%d",
                name, hp, maxHp, atk, skillCooldown);
    }

}
