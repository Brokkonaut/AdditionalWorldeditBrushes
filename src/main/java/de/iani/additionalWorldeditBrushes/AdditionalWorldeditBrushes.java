package de.iani.additionalWorldeditBrushes;

import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.command.tool.BrushTool;
import com.sk89q.worldedit.command.tool.InvalidToolBindException;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class AdditionalWorldeditBrushes extends JavaPlugin {
    private WorldEditPlugin we;
    private int maxRadius = 10;

    @Override
    public void onEnable() {
        we = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");
        getCommand("abrush").setTabCompleter(this);
        CommandExecutor command = new ReplaceBiomeCommand(this);
        getCommand("replacebiome").setExecutor(command);
        getCommand("replacebiome").setTabCompleter((TabCompleter) command);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        ArrayList<String> rv = new ArrayList<>();
        if (args.length == 1) {
            String start = args[0].toLowerCase();
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
            sender.sendMessage(Component.text("Only for players", NamedTextColor.DARK_RED));
            return true;
        }
        if (!sender.hasPermission("additionalWorldEditBrushes.use")) {
            sender.sendMessage(Component.text("Not allowed!", NamedTextColor.DARK_RED));
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
                sender.sendMessage(Component.text("Invalid radius", NamedTextColor.DARK_RED));
                return true;
            }
            if (radius > maxRadius) {
                sender.sendMessage(Component.text("Max radius = " + maxRadius, NamedTextColor.DARK_RED));
                radius = maxRadius;
            }
        }

        Player player = (Player) sender;
        BukkitPlayer wePlayer = we.wrapPlayer(player);
        ItemType inHand;
        if (wePlayer.getItemInHand(HandSide.MAIN_HAND) == null || (inHand = wePlayer.getItemInHand(HandSide.MAIN_HAND).getType()) == ItemTypes.AIR) {
            sender.sendMessage(Component.text("You have no item in your hand", NamedTextColor.DARK_RED));
            return true;
        }
        try {
            if (type.equalsIgnoreCase("pull")) {
                BrushTool tool = we.getSession(player).forceBrush(inHand, new PullBrush(this, player.getUniqueId()), "additionalWorldEditBrushes.use");
                tool.setSize(radius);
                wePlayer.print(TextComponent.of(String.format("Pull brush equipped (Radius %1$.0f).", radius)).color(TextColor.LIGHT_PURPLE));
            } else if (type.equalsIgnoreCase("push")) {
                BrushTool tool = we.getSession(player).forceBrush(inHand, new PushBrush(this, player.getUniqueId()), "additionalWorldEditBrushes.use");
                tool.setSize(radius);
                wePlayer.print(TextComponent.of(String.format("Push brush equipped (Radius %1$.0f).", radius)).color(TextColor.LIGHT_PURPLE));
            } else if (type.equalsIgnoreCase("smooth")) {
                int bias = args.getNext(0);
                bias = Math.max(Math.min(bias, 8), -8);
                BrushTool tool = we.getSession(player).forceBrush(inHand, new SmoothBrush(this, bias), "additionalWorldEditBrushes.use");
                tool.setSize(radius);
                wePlayer.print(TextComponent.of(String.format("Smooth brush equipped (Radius %1$.0f, Bias %2$d).", radius, bias)).color(TextColor.LIGHT_PURPLE));
            } else if (type.equalsIgnoreCase("snipe")) {
                String patternString = args.getNext(null);
                if (patternString == null) {
                    wePlayer.print(TextComponent.of("Missing pattern").color(TextColor.DARK_RED));
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
                    wePlayer.print(TextComponent.of("Could not parse pattern: " + e.getLocalizedMessage()).color(TextColor.DARK_RED));
                    sender.sendMessage("/abrush snipe <pattern>");
                    return true;
                }
                BrushTool tool = we.getSession(player).forceBrush(inHand, new SnipeBrush(this, player.getUniqueId()), "additionalWorldEditBrushes.use");
                tool.setSize(1);
                tool.setFill(pattern);
                wePlayer.print(TextComponent.of("Snipe brush equipped.").color(TextColor.LIGHT_PURPLE));
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
