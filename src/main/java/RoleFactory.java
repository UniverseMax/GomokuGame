/**
 * 角色工厂。
 * 按角色英文名创建对应角色实例。
 * 当前仅支持 Warrior，Astrologer 和 Paladin 待实现。
 */
public class RoleFactory {

    /** 狂战士标识 */
    public static final String WARRIOR = "Warrior";

    /** 占星师标识 */
    public static final String ASTROLOGER = "Astrologer";

    /** 圣骑士标识 */
    public static final String PALADIN = "Paladin";

    /**
     * 私有构造，禁止实例化工厂。
     */
    private RoleFactory() {
    }

    /**
     * 按角色名创建角色。
     *
     * @param roleName 角色英文名，如 "Warrior"
     * @return 对应角色实例
     * @throws IllegalArgumentException 角色名为 null、空白或未知时抛出
     */
    public static Role create(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            throw new IllegalArgumentException("角色名不能为空");
        }

        switch (roleName) {
            case WARRIOR:
                return new Warrior();
            case ASTROLOGER:
                return new Astrologer();
            case PALADIN:
                return new Paladin();
            default:
                throw new IllegalArgumentException("未知角色: " + roleName);
        }
    }
}