package dev.jonz94.dutchytpazhtw;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class CommandHandler implements CommandExecutor {
  private Tpa plugin;

  public CommandHandler(Tpa plugin) {
	this.plugin = plugin;
  }

  static HashMap<UUID, UUID> targetMap = new HashMap<>();

  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
	if (!(sender instanceof Player)) {
	  sender.sendMessage(ChatColor.RED + "只有玩家才可以使用這個指令");
	  return true;
	}
	if (command.getName().equals("tpa")) {
	  if (args.length == 1) {
		if (!Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(args[0]))) {
		  sender.sendMessage(ChatColor.RED + "找不到此玩家，你可能打錯名字或是他目前不在線上");
		  return true;
		}
		Player target = Bukkit.getPlayer(args[0]);
		final Player senderP = (Player)sender;
		if (target.getUniqueId().equals(senderP.getUniqueId())) {
		  sender.sendMessage(ChatColor.RED + "你為什麼想要發出傳送請求給你自己？？？");
		  return true;
		}
		if (targetMap.containsKey(senderP.getUniqueId())) {
		  sender.sendMessage(ChatColor.GOLD + "你已經發過傳送請求了");
		  return false;
		}
		target.sendMessage(
			ChatColor.RED + senderP.getName() + ChatColor.GOLD + " 想要傳送到你目前所在的位置\n你可以使用指令 " + ChatColor.RED + "/tpyes" + ChatColor.GOLD + " 或 " + ChatColor.RED + "/tpaccept" + ChatColor.GOLD + " 接受傳送請求\n你可以使用指令 " + ChatColor.RED + "/tpno" + ChatColor.GOLD + " 或 " + ChatColor.RED + "/tpdeny" + ChatColor.GOLD + " 拒絕傳送請求\n此請求會在 5 分鐘之後自動忽略");
		targetMap.put(senderP.getUniqueId(), target.getUniqueId());
		sender.sendMessage(ChatColor.GOLD + "已經向 " + ChatColor.RED + target.getName() + ChatColor.GOLD + " 發出傳送請求");
		(new BukkitRunnable() {
			public void run() {
			  CommandHandler.targetMap.remove(senderP.getUniqueId());
			}
		  }).runTaskLaterAsynchronously((Plugin)this.plugin, 6000L);
	  } else {
		sender.sendMessage(ChatColor.RED + "指令的格式有誤！");
	  }
	  return true;
	}
	if (command.getName().equals("tpaccept") || command.getName().equals("tpyes")) {
	  final Player senderP = (Player)sender;
	  if (targetMap.containsValue(senderP.getUniqueId())) {
		for (Map.Entry<UUID, UUID> entry : targetMap.entrySet()) {
		  if (((UUID)entry.getValue()).equals(senderP.getUniqueId())) {
			Player tpRequester = Bukkit.getPlayer(entry.getKey());
			SuccessfulTpaEvent event = new SuccessfulTpaEvent(tpRequester, tpRequester.getLocation());
			Bukkit.getPluginManager().callEvent(event);
			tpRequester.teleport((Entity)senderP);
			sender.sendMessage(ChatColor.GOLD + "你接受了 " + ChatColor.RED + tpRequester.getDisplayName() + ChatColor.GOLD + " 的傳送請求");
			tpRequester.sendMessage(ChatColor.RED + sender.getName() + ChatColor.GOLD + " 接受了你的傳送請求");
			targetMap.remove(entry.getKey());
			break;
		  }
		}
	  } else {
		sender.sendMessage(ChatColor.GOLD + "你目前沒有任何傳送請求");
	  }
	  return true;
	}
	if (command.getName().equals("tpdeny") || command.getName().equals("tpno")) {
	  final Player senderP = (Player)sender;
	  if (targetMap.containsValue(senderP.getUniqueId())) {
		for (Map.Entry<UUID, UUID> entry : targetMap.entrySet()) {
		  if (((UUID)entry.getValue()).equals(senderP.getUniqueId())) {
			targetMap.remove(entry.getKey());
			Player originalSender = Bukkit.getPlayer(entry.getKey());
			originalSender.sendMessage(ChatColor.RED + sender.getName() + ChatColor.GOLD + " 拒絕了你的傳送請求");
			sender.sendMessage(ChatColor.GOLD + "你已拒絕了 " + ChatColor.RED + originalSender.getDisplayName() + ChatColor.GOLD + " 的傳送請求");
			break;
		  }
		}
	  } else {
		sender.sendMessage(ChatColor.GOLD + "你目前沒有任何傳送請求");
	  }
	  return true;
	}
	return false;
  }
}