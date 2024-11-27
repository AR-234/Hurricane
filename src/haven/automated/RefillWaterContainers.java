package haven.automated;

import haven.*;
import haven.resutil.WaterTile;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RefillWaterContainers implements Runnable {
    private static final Coord2d posres = Coord2d.of(0x1.0p-10, 0x1.0p-10).mul(11, 11);
    private GameUI gui;

    // To support additional water containers add them here
    private final Map<String, Float> WATER_CONTAINERS = Map.ofEntries(
        // Generic
        Map.entry("gfx/invobjs/waterflask",      2.0F),
        // Inventory
        Map.entry("gfx/invobjs/waterskin",       3.0F),
        Map.entry("gfx/invobjs/glassjug",        5.0F),
        // Belt
        Map.entry("gfx/invobjs/small/waterskin", 3.0F),
        Map.entry("gfx/invobjs/small/glassjug",  5.0F)

    );

    public RefillWaterContainers(GameUI gui) {
        this.gui = gui;
    }

    @Override
    public void run() {
        try {
            do {
                MCache mcache = gui.ui.sess.glob.map;
                int t = mcache.gettile(gui.map.player().rc.floor(MCache.tilesz));
                Tiler tl = mcache.tiler(t);
                if (tl instanceof WaterTile) {
                    Resource res = mcache.tilesetr(t);
                    if (res != null) {
                        if (res.name.equals("gfx/tiles/water") || res.name.equals("gfx/tiles/deep")) {
                            Inventory playerInventory = gui.maininv;
                            refillInventoryWaterContainers(playerInventory);
                            refillInventoryWaterContainers(returnBelt());
                        } else if (res.name.equals("gfx/tiles/owater") || res.name.equals("gfx/tiles/odeep") || res.name.equals("gfx/tiles/odeeper")){
                            gui.ui.error("Refill Water Script: This is salt water, you can't drink this!");
                            return;
                        }
                    } else {
                        gui.ui.error("Refill Water Script: Error checking tile, try again!");
                        return;
                    }
                } else {
                    gui.ui.error("Refill Water Script: You must be on a water tile, in order to refill your containers!");
                    return;
                }
            } while (getInventoryContainers(gui.maininv).size() != 0 || getInventoryContainers(returnBelt()).size() != 0);
            gui.ui.msg("Water Refilled!");
        } catch (Exception e) {
//            gui.ui.error("Refill Water Containers Script: An Unknown Error has occured.");
        }
    }

    private Inventory returnBelt() {
        Inventory belt = null;
        for (Widget w = gui.lchild; w != null; w = w.prev) {
            if (!(w instanceof GItem.ContentsWindow) || !((GItem.ContentsWindow) w).myOwnEquipory) continue;
            if (!((GItem.ContentsWindow) w).cap.contains("Belt")) continue;
            for (Widget ww : w.children()) {
                if (!(ww instanceof Inventory)) continue;
                belt = (Inventory) ww;
            }
        }
        return belt;
    }

    private Map<WItem, Coord> getInventoryContainers(Inventory inventory) {
        Coord inventorySize = inventory.isz;
        Coord sqsz = Inventory.sqsz;
        Map<WItem, Coord> containers = new HashMap<>();
        for (int i = 0; i < inventorySize.x; i++) {
            for (int j = 0; j < inventorySize.y; j++) {
                Coord indexCoord = new Coord(i, j);
                Coord calculatedCoord = indexCoord.mul(sqsz).add(1, 1);
                for (Map.Entry<GItem, WItem> entry : inventory.wmap.entrySet()) {
                    if (entry.getValue().c.equals(calculatedCoord)) {
                        String resName = entry.getKey().res.get().name;
                        ItemInfo.Contents.Content content = getContent(entry.getKey());
                        if(WATER_CONTAINERS.containsKey(resName) && shouldAddToContainers(content, WATER_CONTAINERS.get(resName))){
                            containers.put(entry.getValue(), indexCoord);
                        }
                    }
                }
            }
        }
        return containers;
    }
    private void refillInventoryWaterContainers(Inventory inventory){
        Map<WItem, Coord> inventoryItems = getInventoryContainers(inventory);
        for (Map.Entry<WItem, Coord> item : inventoryItems.entrySet()) {
            try {
                item.getKey().item.wdgmsg("take", Coord.z);
                Thread.sleep(5);
                gui.map.wdgmsg("itemact", Coord.z, gui.map.player().rc.floor(posres), 0);
                Thread.sleep(30);
                inventory.wdgmsg("drop", item.getValue());
                Thread.sleep(5);
            } catch (InterruptedException ignored) {
                return;
            }
        }
    }
    private ItemInfo.Contents.Content getContent(GItem item) {
        ItemInfo.Contents.Content content = null;
        for (ItemInfo info : item.info()) {
            if (info instanceof ItemInfo.Contents) {
                content = ((ItemInfo.Contents) info).content;
            }
        }
        return content;
    }

    private boolean shouldAddToContainers(ItemInfo.Contents.Content content, float contentCount) {
        return content == null || (content.count != contentCount && Objects.equals(content.name, "Water"));
    }
}
