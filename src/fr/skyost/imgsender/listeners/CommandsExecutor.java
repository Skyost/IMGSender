package fr.skyost.imgsender.listeners;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skyost.imgsender.tasks.IMGTask;

public class CommandsExecutor implements CommandExecutor {
	
	@Override
	public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
		String url = null;
		String size = null;
		String imageChar = null;
		String[] text = null;
		Player player = null;
		ItemStack itemStack = null;
		if(cmd.getName().equalsIgnoreCase("img")) {
			if(args.length > 0) {
				url = args[0];
				if(args.length > 1) {
					size = args[1];
					if(args.length > 2) {
						imageChar = args[2];
						if(args.length > 3) {
							player = Bukkit.getPlayer(args[args.length - 1]);
							text = Arrays.copyOfRange(args, 3, (player == null ? args.length : args.length - 1));
						}
					}
				}
			}
		}
		else {
			if(!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "You must be a player !");
				return true;
			}
			itemStack = ((Player)sender).getItemInHand();
			if(itemStack == null || itemStack.getType() == Material.AIR) {
				sender.sendMessage(ChatColor.RED + "Nothing in your hand !");
				return true;
			}
			if(args.length > 0) {
				url = args[0];
				if(args.length > 1) {
					size = args[1];
					if(args.length > 2) {
						imageChar = args[2];
						if(args.length > 3) {
							text = Arrays.copyOfRange(args, 3, args.length);
						}
					}
				}
			}
		}
		new IMGTask(sender, url, size, imageChar, text, player, itemStack).start();
		return true;
	}
	
}
