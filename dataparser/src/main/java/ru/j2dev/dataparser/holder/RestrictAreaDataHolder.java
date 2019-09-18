package ru.j2dev.dataparser.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.dataparser.annotations.Element;
import ru.j2dev.dataparser.holder.areadata.AreaType;
import ru.j2dev.dataparser.holder.areadata.area.BlockedActionsZone;
import ru.j2dev.dataparser.holder.areadata.area.DefaultArea;
import ru.j2dev.dataparser.holder.areadata.area.DefaultArea.OnOffZoneParam;
import ru.j2dev.dataparser.holder.restrictarea.RestrictAreaData;

import java.util.ArrayList;
import java.util.List;

/**
 * @author KilRoy
 */
public class RestrictAreaDataHolder extends AbstractHolder {
    private static final RestrictAreaDataHolder ourInstance = new RestrictAreaDataHolder();
    @Element(start = "no_call_pc_begin", end = "no_call_pc_end")
    private List<RestrictAreaData> noCallPc;
    @Element(start = "no_drop_item_begin", end = "no_drop_item_end")
    private List<RestrictAreaData> noDropItem;
    @Element(start = "nofly_begin", end = "nofly_end")
    private List<RestrictAreaData> noFly;
    @Element(start = "no_save_bookmark_begin", end = "no_save_bookmark_end")
    private List<RestrictAreaData> noSaveBookmark;
    @Element(start = "no_use_bookmark_begin", end = "no_use_bookmark_end")
    private List<RestrictAreaData> noUseBookmark;
    @Element(start = "transformable_begin", end = "transformable_end")
    private List<RestrictAreaData> transformable;
    private final List<DefaultArea> blockedActionsAreasData;

    public RestrictAreaDataHolder() {
        blockedActionsAreasData = new ArrayList<>();
    }

    public static RestrictAreaDataHolder getInstance() {
        return ourInstance;
    }

    @Override
    public void afterParsing() {
        super.afterParsing();
        // Инициализация ограничений на зоны
        noCallPc.forEach(areas ->
        {
            final BlockedActionsZone restrictArea = new BlockedActionsZone();
            restrictArea.name = areas.getName();
            restrictArea.default_status = OnOffZoneParam.on;
            restrictArea.type = AreaType.neutral;
            restrictArea.ranges = areas.getRestrictedPoints();
            restrictArea.addBlockedActions("no_call_pc");
            blockedActionsAreasData.add(restrictArea);
        });
        noDropItem.forEach(areas ->
        {
            final BlockedActionsZone restrictArea = new BlockedActionsZone();
            restrictArea.name = areas.getName();
            restrictArea.default_status = OnOffZoneParam.on;
            restrictArea.type = AreaType.neutral;
            restrictArea.ranges = areas.getRestrictedPoints();
            restrictArea.addBlockedActions("no_drop_item");
            blockedActionsAreasData.add(restrictArea);
        });
        noFly.forEach(areas ->
        {
            final BlockedActionsZone restrictArea = new BlockedActionsZone();
            restrictArea.name = areas.getName();
            restrictArea.default_status = OnOffZoneParam.on;
            restrictArea.type = AreaType.neutral;
            restrictArea.ranges = areas.getRestrictedPoints();
            restrictArea.addBlockedActions("nofly");
            blockedActionsAreasData.add(restrictArea);
        });
        noSaveBookmark.forEach(areas ->
        {
            final BlockedActionsZone restrictArea = new BlockedActionsZone();
            restrictArea.name = areas.getName();
            restrictArea.default_status = OnOffZoneParam.on;
            restrictArea.type = AreaType.neutral;
            restrictArea.ranges = areas.getRestrictedPoints();
            restrictArea.addBlockedActions("no_save_bookmark");
            blockedActionsAreasData.add(restrictArea);
        });
        noUseBookmark.forEach(areas ->
        {
            final BlockedActionsZone restrictArea = new BlockedActionsZone();
            restrictArea.name = areas.getName();
            restrictArea.default_status = OnOffZoneParam.on;
            restrictArea.type = AreaType.neutral;
            restrictArea.ranges = areas.getRestrictedPoints();
            restrictArea.addBlockedActions("no_use_bookmark");
            blockedActionsAreasData.add(restrictArea);
        });
        transformable.forEach(areas ->
        {
            final BlockedActionsZone restrictArea = new BlockedActionsZone();
            restrictArea.name = areas.getName();
            restrictArea.default_status = OnOffZoneParam.on;
            restrictArea.type = AreaType.neutral;
            restrictArea.ranges = areas.getRestrictedPoints();
            restrictArea.addBlockedActions("transformable");
            blockedActionsAreasData.add(restrictArea);
        });
    }

    @Override
    public int size() {
        return noCallPc.size() +
                noDropItem.size() +
                noFly.size() +
                noSaveBookmark.size() +
                noUseBookmark.size() +
                transformable.size();
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub
    }
}