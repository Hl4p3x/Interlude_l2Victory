package ru.j2dev.dataparser.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.dataparser.annotations.Element;
import ru.j2dev.dataparser.annotations.factory.IObjectFactory;
import ru.j2dev.dataparser.holder.doordata.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.j2dev.dataparser.holder.doordata.ObserverSignBoard.ObserverGroup;

/**
 * @author : Camelion
 * @date : 26.08.12 22:29
 */
public class DoorDataHolder extends AbstractHolder {
    private static final Pattern observerGroupPattern = Pattern.compile("observer_group\\s*=\\s*(\\d+)", Pattern.DOTALL);
    private static final Pattern observerPosPattern = Pattern.compile("observer_pos\\s*=\\s*\\{([\\d;-]+)}", Pattern.DOTALL);
    private static final DoorDataHolder ourInstance = new DoorDataHolder();
    @Element(start = "door_begin", end = "door_end")
    private List<Door> doors;
    @Element(start = "chair_begin", end = "chair_end")
    private List<Chair> chairs;
    @Element(start = "signboard_begin", end = "signboard_end", objectFactory = SignBoardObjectFactory.class)
    private List<DefaultSignBoard> signBoards;
    @Element(start = "controltower_begin", end = "controltower_end")
    private List<ControlTower> controlTowers;

    private DoorDataHolder() {
    }

    public static DoorDataHolder getInstance() {
        return ourInstance;
    }

    @Override
    public int size() {
        return doors.size() + chairs.size() + signBoards.size() + controlTowers.size();
    }

    public List<DefaultSignBoard> getSignBoards() {
        return signBoards;
    }

    public List<Door> getDoors() {
        return doors;
    }

    public Door getDoorByName(final String name) {
        return doors.stream().filter(doors -> doors.door_name.equalsIgnoreCase(name)).findFirst().get();
    }

    public List<Chair> getChairs() {
        return chairs;
    }

    public List<ControlTower> getControlTowers() {
        return controlTowers;
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub
    }

    public static class SignBoardObjectFactory implements IObjectFactory<DefaultSignBoard> {
        @Override
        public DefaultSignBoard createObjectFor(StringBuilder data) {
            DefaultSignBoard defaultSignBoard;
            if (data.indexOf("observer_group") > 0) {
                defaultSignBoard = new ObserverSignBoard();
            } else {
                return new SignBoard();
            }
            Matcher matcher = observerGroupPattern.matcher(data);
            ObserverSignBoard board = (ObserverSignBoard) defaultSignBoard;
            board.observers = new ArrayList<>();
            while (matcher.find()) {
                ObserverGroup observerGroup = new ObserverGroup();
                board.observers.add(observerGroup);
                observerGroup.observer_group = Integer.valueOf(matcher.group(1));
                observerGroup.observer_poses = new ArrayList<>();
                StringBuilder localData = new StringBuilder();
                data.delete(matcher.start(), matcher.end());
                int nextIndex = data.indexOf("observer_group");
                if (nextIndex > 0) {
                    localData.append(data.substring(matcher.start(), nextIndex));
                } else {
                    localData.append(data.substring(matcher.start(), data.length()));
                }
                Matcher posMatcher = observerPosPattern.matcher(localData);
                while (posMatcher.find()) {
                    String[] parts = posMatcher.group(1).split(";");
                    int[] poses = new int[parts.length];
                    for (int i = 0; i < parts.length; i++)
                        poses[i] = Integer.valueOf(parts[i]);
                    observerGroup.observer_poses.add(poses);
                }
                matcher = observerGroupPattern.matcher(data);
            }
            return defaultSignBoard;
        }

        @Override
        public void setFieldClass(Class<?> clazz) {
            // Ignore
        }
    }
}