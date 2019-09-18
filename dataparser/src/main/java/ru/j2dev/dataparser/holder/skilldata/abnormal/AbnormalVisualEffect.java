package ru.j2dev.dataparser.holder.skilldata.abnormal;

/**
 * @author KilRoy
 */
public enum AbnormalVisualEffect {
    ave_none(0x0000000, AbnormalVisualEffectType.NORMAL),
    ave_stun(0x00000040, AbnormalVisualEffectType.NORMAL),
    ave_dot_poison(0x00000002, AbnormalVisualEffectType.NORMAL),
    ave_sleep(0x00000080, AbnormalVisualEffectType.NORMAL),
    ave_root(0x00000200, AbnormalVisualEffectType.NORMAL),
    ave_dot_bleeding(0x00000001, AbnormalVisualEffectType.NORMAL),
    ave_turn_flee(0x80000000, AbnormalVisualEffectType.NORMAL),
    ave_ultimate_defence(0x08000000, AbnormalVisualEffectType.NORMAL),
    ave_stealth(0x00100000, AbnormalVisualEffectType.NORMAL),
    ave_paralyze(0x00000400, AbnormalVisualEffectType.NORMAL),
    ave_silence(0x00000100, AbnormalVisualEffectType.NORMAL),
    ave_flesh_stone(0x00000800, AbnormalVisualEffectType.NORMAL),
    ave_death_mark(0x40000000, AbnormalVisualEffectType.NORMAL),
    ave_real_target(0x20000000, AbnormalVisualEffectType.NORMAL),
    ave_big_head(0x00002000, AbnormalVisualEffectType.NORMAL),
    ave_vp_up(0x10000000, AbnormalVisualEffectType.NORMAL),
    ave_vp_keep(0x10000000, AbnormalVisualEffectType.NORMAL), // Not find
    ave_magic_square(0x00800000, AbnormalVisualEffectType.NORMAL),
    ave_big_body(0x00010000, AbnormalVisualEffectType.NORMAL),
    ave_change_texture(0x00008000, AbnormalVisualEffectType.NORMAL),
    ave_dance_root(0x00040000, AbnormalVisualEffectType.NORMAL),
    ave_ghost_stun(0x00080000, AbnormalVisualEffectType.NORMAL),
    ave_floating_root(0x00020000, AbnormalVisualEffectType.NORMAL),
    ave_seizure1(0x00200000, AbnormalVisualEffectType.NORMAL),
    ave_seizure2(0x00400000, AbnormalVisualEffectType.NORMAL),
    ave_shake(0x02000000, AbnormalVisualEffectType.NORMAL),

    ave_time_bomb(0x004000, AbnormalVisualEffectType.SPECIAL), // High Five
    ave_mp_shield(0x008000, AbnormalVisualEffectType.SPECIAL), // High Five
    ave_navit_advent(0x080000, AbnormalVisualEffectType.SPECIAL), // High Five
    ave_invincibility(0x000001, AbnormalVisualEffectType.SPECIAL),
    ave_air_battle_slow(0x000002, AbnormalVisualEffectType.SPECIAL),
    ave_air_battle_root(0x000004, AbnormalVisualEffectType.SPECIAL),
    ave_stigma_of_silen(0x000100, AbnormalVisualEffectType.SPECIAL),
    ave_change_wp(0x000008, AbnormalVisualEffectType.SPECIAL),
    ave_change_hair_b(0x000040, AbnormalVisualEffectType.SPECIAL),
    ave_change_hair_g(0x000010, AbnormalVisualEffectType.SPECIAL),
    ave_change_hair_p(0x000020, AbnormalVisualEffectType.SPECIAL),
    ave_change_ves_s(0x000800, AbnormalVisualEffectType.SPECIAL),
    ave_change_ves_c(0x001000, AbnormalVisualEffectType.SPECIAL),
    ave_change_ves_d(0x002000, AbnormalVisualEffectType.SPECIAL),
    ave_speed_down(0x000200, AbnormalVisualEffectType.SPECIAL),
    ave_frozen_pillar(0x000400, AbnormalVisualEffectType.SPECIAL),
    br_ave_afro_normal(0x000001, AbnormalVisualEffectType.EVENT),
    br_ave_afro_gold(0x000004, AbnormalVisualEffectType.EVENT),
    br_ave_afro_pink(0x000002, AbnormalVisualEffectType.EVENT),
    br_ave_vesper1(0x000020, AbnormalVisualEffectType.EVENT),
    br_ave_vesper2(0x000040, AbnormalVisualEffectType.EVENT),
    br_ave_vesper3(0x000080, AbnormalVisualEffectType.EVENT),
    br_ave_soul_avatar(0x000100, AbnormalVisualEffectType.EVENT),
    br_ave_power_of_eva(0x000008, AbnormalVisualEffectType.EVENT);

    private final int mask; // Special ID abn
    private final AbnormalVisualEffectType type; // 0 Normal, 1 Special, 2 Event

    AbnormalVisualEffect(final int mask, final AbnormalVisualEffectType type) {
        this.mask = mask;
        this.type = type;
    }

    public int getMask() {
        return mask;
    }

    public AbnormalVisualEffectType getType() {
        return type;
    }
}