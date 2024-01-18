package com.adamcalculator.cheststofox.container.stat;

import com.adamcalculator.cheststofox.container.ContainerEntry;
import com.adamcalculator.cheststofox.container.ContainersData;
import com.adamcalculator.cheststofox.util.Util;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class StatCollector {
    public static HashMap<String, StatRow> collectAvailable(ContainersData containersData) {
        final HashMap<String, StatRow> stat = new HashMap<>();

        for (BlockPos chestPosition : containersData.getSavedPositions()) {
            ContainerEntry chest = containersData.getContainer(chestPosition);

            chest.iterate((slot, itemEntry) -> {
                String id = itemEntry.getId();
                int amount = itemEntry.getCount();
                StatRow row = stat.get(id);
                if (row == null) {
                    row = new StatRow(id);
                    row.setLocalized(itemEntry.getLocalized());
                    stat.put(id, row);
                }
                row.increaseAmount(amount, ContainersData.posToKey(chestPosition));
            });
        }


        return Util.sortHashmapByValues(stat, Comparator.comparingInt(o -> -o.amount));
    }

    public static HashMap<String, StatRow> collectAll(ContainersData containersData) {
        final Identifier airIdentifier = Registries.ITEM.getId(Items.AIR);
        final HashMap<String, StatRow> stat = new HashMap<>();

        for (final Identifier id : Registries.ITEM.getIds()) {
            if (id.equals(airIdentifier)) {
                continue;
            }
            String strId = id.getPath();

            StatRow row = new StatRow(strId);
            row.setLocalized(Registries.ITEM.get(id).getName().asTruncatedString(99));
            stat.put(strId, row);
        }

        for (BlockPos chestPosition : containersData.getSavedPositions()) {
            ContainerEntry chest = containersData.getContainer(chestPosition);

            chest.iterate((slot, itemEntry) -> {
                String id = itemEntry.getId();
                int amount = itemEntry.getCount();
                StatRow row = stat.get(id);
                row.increaseAmount(amount, ContainersData.posToKey(chestPosition));
            });
        }

        return Util.sortHashmapByKeys(stat, String::compareToIgnoreCase);
    }

    public static String statToCsv(HashMap<String, StatRow> stat) {
        StringBuilder sb = new StringBuilder("localized,id,amount\n");
        for (StatRow row : stat.values()) {

            String localized = row.localized;
            sb.append(localized).append(",").append(row.id).append(",").append(row.amount);
            sb.append("\n");
        }

        return sb.toString();
    }

    public static class StatRow {
        public String localized = "<noname>";
        String id;
        int amount;
        final Set<String> containers = new HashSet<>();

        public StatRow(String id) {
            this.id = id;
        }

        public void setLocalized(String localized) {
            this.localized = localized;
        }

        public void increaseAmount(int offset, String chest) {
            amount += offset;
            containers.add(chest);
        }

        public Set<String> getContainers() {
            return containers;
        }
    }
}
