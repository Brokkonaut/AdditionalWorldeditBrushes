package de.iani.additionalWorldeditBrushes;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.FlatRegionFunction;
import com.sk89q.worldedit.function.FlatRegionMaskingFilter;
import com.sk89q.worldedit.function.biome.BiomeReplace;
import com.sk89q.worldedit.function.mask.Mask2D;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.visitor.FlatRegionVisitor;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.Regions;
import com.sk89q.worldedit.world.biome.BaseBiome;
import com.sk89q.worldedit.world.biome.BiomeData;
import com.sk89q.worldedit.world.registry.BiomeRegistry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class ReplaceBiomeCommand implements CommandExecutor, TabCompleter {

    private AdditionalWorldeditBrushes plugin;
    private HashMap<BaseBiome, String> biomeToName = new HashMap<>();
    private HashMap<String, BaseBiome> nameToBiome = new HashMap<>();

    public ReplaceBiomeCommand(AdditionalWorldeditBrushes plugin) {
        this.plugin = plugin;

        BiomeRegistry biomeRegistry = WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.GAME_HOOKS).getRegistries().getBiomeRegistry();
        List<BaseBiome> biomes = biomeRegistry.getBiomes();
        for (BaseBiome biome : biomes) {
            BiomeData data = biomeRegistry.getData(biome);
            if (data != null) {
                String biomeName = data.getName();
                biomeToName.put(biome, biomeName);
                nameToBiome.put(biomeName, biome);
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.DARK_RED + "Only for players!");
            return true;
        }
        if (!sender.hasPermission("worldedit.biome.set")) {
            sender.sendMessage(ChatColor.DARK_RED + "No permission!");
            return true;
        }
        Player player = (Player) sender;

        if (args.length < 2) {
            sender.sendMessage("/replacebiome <newbiome> <radius> [oldbiome]");
            return true;
        }
        String biomeName = args[0].toUpperCase();
        BaseBiome biome = nameToBiome.get(biomeName);
        if (biome == null) {
            sender.sendMessage(ChatColor.DARK_RED + "Unknown Biome!");
            return true;
        }
        int radius = -1;
        try {
            radius = Integer.parseInt(args[1]);
        } catch (IllegalArgumentException e) {
        }
        if (radius < 1 || radius > 60) {
            sender.sendMessage(ChatColor.DARK_RED + "Invalid radius (1..60)!");
            return true;
        }
        int expandedRadius = (int) ((radius + 1) * 1.3);
        BaseBiome oldBiome = null;
        if (args.length >= 3) {
            oldBiome = nameToBiome.get(args[2]);
            if (oldBiome == null) {
                sender.sendMessage(ChatColor.DARK_RED + "Unknown old Biome!");
                return true;
            }
        }

        BukkitPlayer wePlayer = plugin.getWorldEdit().wrapPlayer(player);
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(wePlayer);
        EditSession editSession = session.createEditSession(wePlayer);

        Region region = new CuboidRegion(wePlayer.getLocation().toVector().toBlockPoint().subtract(expandedRadius, 0, expandedRadius), wePlayer.getLocation().toVector().toBlockPoint().add(expandedRadius, 0, expandedRadius));
        FlatRegionFunction replace = new BiomeReplace(editSession, biome);

        // Mask mask = editSession.getMask();
        // Mask2D mask2d = mask != null ? mask.toMask2D() : null;
        // if (mask2d != null) {
        // replace = new FlatRegionMaskingFilter(mask2d, replace);
        // }
        BlockVector2 center = wePlayer.getLocation().toVector().toBlockPoint().toBlockVector2();
        if (oldBiome == null) {
            oldBiome = editSession.getBiome(center);
        }
        if (oldBiome.equals(biome)) {
            sender.sendMessage(ChatColor.DARK_RED + "Old biome and new biome are the same!");
            return true;
        }

        replace = new FlatRegionMaskingFilter(new RadiusMask2D(player, editSession, center, radius, oldBiome), replace);
        FlatRegionVisitor visitor = new FlatRegionVisitor(Regions.asFlatRegion(region), replace);
        try {
            Operations.completeLegacy(visitor);
        } catch (MaxChangedBlocksException e) {
            wePlayer.print("Max blocks limit reached!");
        }

        wePlayer.print("Biomes were changed in " + visitor.getAffected() + " columns. You may have to rejoin your game (or close and reopen your world) to see a change.");

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        ArrayList<String> result = new ArrayList<>();
        String last = args.length == 0 ? "" : args[args.length - 1];
        if (args.length == 1 || args.length == 3) {
            for (String s : nameToBiome.keySet()) {
                if (s.toLowerCase().startsWith(last.toLowerCase())) {
                    result.add(s);
                }
            }
        }
        return result;
    }

    private class RadiusMask2D implements Mask2D {
        private Player player;
        private Extent extent;
        private BlockVector2 center;
        private int radius;
        private BaseBiome expectedBiome;
        private double add1;
        private double add2;
        private double add3;

        public RadiusMask2D(Player player, Extent extent, BlockVector2 center, int radius, BaseBiome expectedBiome) {
            this.player = player;
            this.extent = extent;
            this.center = center;
            this.radius = radius;
            this.expectedBiome = expectedBiome;
            Random random = new Random();
            add1 = random.nextDouble() * Math.PI * 2;
            add2 = random.nextDouble() * Math.PI * 2;
            add3 = random.nextDouble() * Math.PI * 2;
        }

        @Override
        public boolean test(BlockVector2 vector) {
            BaseBiome oldBiome = extent.getBiome(vector);
            if (!oldBiome.equals(expectedBiome)) {
                return false;
            }
            BlockVector2 relative = vector.subtract(center);
            double d = relative.length();
            double atan = Math.atan2(relative.getZ(), relative.getX());// -pi ... pi
            double maxd = 1;
            maxd += Math.sin(atan * 2 + add1) * 0.1;// Magic value!
            maxd += Math.sin((atan * 5) + add2) * 0.07;// Magic value!
            maxd += Math.sin((atan * 11) + add3) * 0.05;// Magic value!
            if (d > maxd * radius) {
                return false;
            }

            Location loc = new Location(player.getWorld(), vector.getBlockX(), player.getWorld().getHighestBlockAt(vector.getBlockX(), vector.getBlockZ()).getY() + 2, vector.getBlockZ());
            player.sendBlockChange(loc, Material.RED_STAINED_GLASS.createBlockData());
            return true;
        }

    }

}
