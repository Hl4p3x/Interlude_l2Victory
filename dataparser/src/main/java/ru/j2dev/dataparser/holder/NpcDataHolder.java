package ru.j2dev.dataparser.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.dataparser.annotations.Element;
import ru.j2dev.dataparser.annotations.factory.IObjectFactory;
import ru.j2dev.dataparser.holder.npcdata.*;
import ru.j2dev.dataparser.pch.LinkerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author : Camelion
 * @date : 30.08.12 14:44
 */
public class NpcDataHolder extends AbstractHolder {
    private static final NpcDataHolder ourInstance = new NpcDataHolder();
    @Element(start = "npc_begin", end = "npc_end", objectFactory = NpcDataFactory.class)
    public List<NpcData> npcDatas;

    private NpcDataHolder() {
    }

    public static NpcDataHolder getInstance() {
        return ourInstance;
    }

    @Override
    public int size() {
        return npcDatas.size();
    }

    public NpcData getNpcData(final int npcId) {
        final Optional<NpcData> dataOptional = npcDatas.stream().filter(npc -> npc.npc_class_id == npcId).findFirst();
        return dataOptional.orElse(null);
    }

    public List<NpcData> getNpcDatas() {
        return npcDatas;
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub
    }

    public static class NpcDataFactory implements IObjectFactory<NpcData> {
        public static final Pattern pattern = Pattern.compile("(\\S++)");

        @Override
        public NpcData createObjectFor(StringBuilder data) {
            Matcher m = pattern.matcher(data);
            if (m.find()) {
                String type = m.group(1);
                switch (type) {
                    case "warrior":
                        return new WarriorData();
                    case "citizen":
                        return new CitizenData();
                    case "treasure":
                        return new TreasureData();
                    case "boss":
                        return new BossData();
                    case "zzoldagu":
                        return new ZzoldaguData();
                    case "world_trap":
                        return new WorldTrapData();
                    case "collection":
                        return new CollectionData();
                    case "merchant":
                        return new MerchantData();
                    case "warehouse_keeper":
                        return new WarehouseKeeperData();
                    case "teleporter":
                        return new TeleporterData();
                    case "guild_master":
                        return new GuildMasterData();
                    case "guild_coach":
                        return new GuildCoachData();
                    case "guard":
                        return new GuardData();
                    case "blacksmith":
                        return new BlacksmithData();
                    case "mrkeeper":
                        return new MrkeeperData();
                    case "monrace":
                        return new MonraceData();
                    case "package_keeper":
                        return new PackageKeeper();
                    case "holything":
                        return new HolythingData();
                    case "siege_attacker":
                        return new SiegeAttackerData();
                    case "ownthing":
                        return new OwnthingData();
                    case "summon":
                        return new SummonData();
                    case "pet":
                        return new PetData();
                    case "xmastree":
                        return new XmasstreeData();
                    case "pc_trap":
                        return new PCTrapData();
                    case "doppelganger":
                        return new DoppelgangerData();
                }
            }
            return null;
        }

        @Override
        public void setFieldClass(Class<?> clazz) {
            // Ignore
        }
    }

    public static class NpcAIObjectFactory implements IObjectFactory<NpcData.NpcDataAI> {
        public static final Pattern aiNamePattern = Pattern.compile("\\[(\\S+?)]");
        public static final Pattern paramsPattern = Pattern.compile("\\{\\[(\\S+?)]\\s*?=\\s*?(\\S+?)}");
        public static final Pattern paramPattern = Pattern.compile("\\b(?<Float>\\-?\\d+\\.\\d+)|(?<Integer>\\-?\\d+)" + "|(?<String>\\[(?<StringVal>\\S+)])|(?<Link>@\\S+)|(?<aString>\\S+)");

        @Override
        public NpcData.NpcDataAI createObjectFor(StringBuilder data) throws NoSuchFieldException {
            Matcher m = aiNamePattern.matcher(data);
            NpcData.NpcDataAI npcDataAI = null;
            Map<String, Object> paramsMap = new HashMap<>();
            String ai_name;
            if (m.find()) {
                ai_name = m.group(1);
                data.delete(m.start(), m.end());
                if (data.indexOf(";") > 0) {// Есть параметры
                    data.delete(0, data.indexOf(";") + 1);
                }
                npcDataAI = new NpcData.NpcDataAI(ai_name, paramsMap);
            }
            m = paramsPattern.matcher(data);
            while (m.find()) {
                String key = m.group(1);
                String value = m.group(2);
                Matcher valueMatcher = paramPattern.matcher(value);
                if (valueMatcher.find()) {
                    String param = valueMatcher.group("Float");
                    if (param != null) {
                        paramsMap.put(key, Float.valueOf(value));
                        continue;
                    }
                    param = valueMatcher.group("Integer");
                    if (param != null) {
                        paramsMap.put(key, Integer.valueOf(value));
                        continue;
                    }
                    param = valueMatcher.group("String");
                    if (param != null) {
                        paramsMap.put(key, valueMatcher.group("StringVal"));
                        continue;
                    }
                    param = valueMatcher.group("Link");
                    if (param != null) {
                        String p = LinkerFactory.getInstance().findValueFor(value);
                        if (p == null)
                            throw new NoSuchFieldException(value);
                        paramsMap.put(key, p);
                        continue;
                    }
                    param = valueMatcher.group("aString");
                    if (param != null) {
                        paramsMap.put(key, value);
                        continue;
                    }
                    throw new NoSuchFieldError();
                }
            }
            return npcDataAI;
        }

        @Override
        public void setFieldClass(Class<?> clazz) {
            // Ignored
        }
    }
}