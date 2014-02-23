package fr.skyost.imgsender.listeners;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.skyost.imgsender.tasks.IMGManager;

public class CommandsExecutor implements CommandExecutor {
	
	@Override
	public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
		String url = null;
		String size = null;
		String imageChar = null;
		String[] text = null;
		Player player = null;
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
		new IMGManager(sender, url, size, imageChar, text, player).start();
		return true;
	}
	
}
