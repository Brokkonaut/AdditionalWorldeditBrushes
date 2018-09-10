package de.iani.additionalWorldeditBrushes;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.command.tool.BrushTool;
import com.sk89q.worldedit.command.tool.InvalidToolBindException;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.world.item.ItemTypes;

public class AdditionalWorldeditBrushes extends JavaPlugin {
    private WorldEditPlugin we;
    private int maxRadius = 10;

    @Override
    public void onEnable() {
        we = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");
        getCommand("abrush").setTabCompleter(this);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        ArrayList<String> rv = new ArrayList<String>();
        if (args.length == 1) {
            String start = args[0].toLowerCase();
            if ("pull".startsWith(start)) {
                rv.add("pull");
            }
            if ("pull".startsWith(start)) {
                rv.add("pull");
            }
            if ("push".startsWith(start)) {
                rv.add("push");
            }
            if ("smooth".startsWith(start)) {
                rv.add("smooth");
            }
            if ("snipe".startsWith(start)) {
                rv.add("snipe");
            }
        }
        return rv;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] argsa) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.DARK_RED + "Only for players");
            return true;
        }
        if (!sender.hasPermission("additionalWorldEditBrushes.use")) {
            sender.sendMessage(ChatColor.DARK_RED + "Not allowed!");
            return true;
        }

        ArgsParser args = new ArgsParser(argsa);
        if (!args.hasNext()) {
            // usage
            sender.sendMessage("/abrush pull [radius]");
            sender.sendMessage("/abrush push [radius]");
            sender.sendMessage("/abrush smooth [radius [bias]]");
            sender.sendMessage("/abrush snipe <pattern>");
            return true;
        }
        String type = args.getNext("");

        double radius = 5;
        if (!type.equalsIgnoreCase("snipe")) {
            if (args.remaining() > 0) {
                radius = args.getNext(-1.0);
            }
            if (radius < 1) {
                sender.sendMessage(ChatColor.DARK_RED + "Invalid radius");
                return true;
            }
            if (radius > maxRadius) {
                sender.sendMessage(ChatColor.DARK_RED + "Max radius = " + maxRadius);
                radius = maxRadius;
            }
        }

        Player player = (Player) sender;
        BukkitPlayer wePlayer = we.wrapPlayer(player);
        if (wePlayer.getItemInHand(HandSide.MAIN_HAND) == null || wePlayer.getItemInHand(HandSide.MAIN_HAND).getType() == ItemTypes.AIR) {
            sender.sendMessage(ChatColor.DARK_RED + "You have no item in your hand");
            return true;
        }
        try {
            if (type.equalsIgnoreCase("pull")) {
                BrushTool tool = we.getSession(player).getBrushTool(wePlayer.getItemInHand(HandSide.MAIN_HAND).getType());
                tool.setBrush(new PullBrush(this, player.getUniqueId()), "additionalWorldEditBrushes.use");
                tool.setSize(radius);
                wePlayer.print(String.format("Pull brush equipped (Radius %1$.0f).", radius));
            } else if (type.equalsIgnoreCase("push")) {
                BrushTool tool = we.getSession(player).getBrushTool(wePlayer.getItemInHand(HandSide.MAIN_HAND).getType());
                tool.setBrush(new PushBrush(this, player.getUniqueId()), "additionalWorldEditBrushes.use");
                tool.setSize(radius);
                wePlayer.print(String.format("Push brush equipped (Radius %1$.0f).", radius));
            } else if (type.equalsIgnoreCase("smooth")) {
                int bias = args.getNext(0);
                bias = Math.max(Math.min(bias, 8), -8);
                BrushTool tool = we.getSession(player).getBrushTool(wePlayer.getItemInHand(HandSide.MAIN_HAND).getType());
                tool.setBrush(new SmoothBrush(this, bias), "additionalWorldEditBrushes.use");
                tool.setSize(radius);
                wePlayer.print(String.format("Smooth brush equipped (Radius %1$.0f, Bias %2$d).", radius, bias));
            } else if (type.equalsIgnoreCase("snipe")) {
                String patternString = args.getNext(null);
                if (patternString == null) {
                    sender.sendMessage(ChatColor.DARK_RED + "Missing pattern");
                    sender.sendMessage("/abrush snipe <pattern>");
                    return true;
                }
                ParserContext context = new ParserContext();
                context.setActor(wePlayer);
                context.setWorld(wePlayer.getWorld());
                context.setSession(we.getSession(player));
                Pattern pattern;
                try {
                    pattern = we.getWorldEdit().getPatternFactory().parseFromInput(patternString, context);
                } catch (InputParseException e) {
                    sender.sendMessage(ChatColor.DARK_RED + "Could not parse pattern: " + e.getLocalizedMessage());
                    sender.sendMessage("/abrush snipe <pattern>");
                    return true;
                }
                BrushTool tool = we.getSession(player).getBrushTool(wePlayer.getItemInHand(HandSide.MAIN_HAND).getType());
                tool.setBrush(new SnipeBrush(this, player.getUniqueId(), tool), "additionalWorldEditBrushes.use");
                tool.setSize(1);
                tool.setFill(pattern);
                wePlayer.print(String.format("Snipe brush equipped."));
            } else {
                sender.sendMessage("Invalid brush");
            }
        } catch (InvalidToolBindException e) {
            sender.sendMessage(e.getLocalizedMessage());
        }

        return true;
    }

    public WorldEditPlugin getWorldEdit() {
        return we;
    }
}
