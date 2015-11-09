package io.github.andorem.playernotify;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

 
public class PlayerNotify extends JavaPlugin {
	
	static File DATA_FOLDER;
	Notification notification;
	ChatListener chatListener;
	Logger log = getLogger();
	boolean sendPingNotification;

	String noPermission = ChatColor.DARK_RED + "You don't have permission to perform this command.";
	String userCommands = ChatColor.RED + "\nAvailable commands:\n"
			+ ChatColor.GREEN + "/pn [username]" + ChatColor.WHITE 
			+ " - Send a notification to a specific user without typing into public chat.\n"
			+ ChatColor.GREEN + "/pn help" + ChatColor.WHITE
			+ " - Show all available PlayerNotify commands. \n"
			+ ChatColor.GREEN + "/pn mute" + ChatColor.WHITE
			+ " - Toggle mute/unmute for incoming notifications.";
	String adminCommands = ChatColor.RED + "\nAdmin commands:\n" 
			+ ChatColor.GREEN + "/pn set [SOUND_NAME]" + ChatColor.WHITE
			+ " - Set the notification sound to be heard by all players. \n"
			+ ChatColor.GOLD + "Refer to http://jd.bukkit.org/org/bukkit/Sound.html\n"
			+ ChatColor.GREEN + "/pn reload" + ChatColor.WHITE
			+ " - Reloads the PlayerNotify configuration file. \n";
	
	@Override
    public void onEnable() {
		DATA_FOLDER = getDataFolder();

        try {
    		if(!DATA_FOLDER.exists()) {
    		    DATA_FOLDER.mkdir();
    		}
            File file = new File(DATA_FOLDER, "config.yml");
            if (!file.exists()) {
               log.info("No config.yml found. Generating default one.");  
               createConfig();
            }
        } 
        catch (Exception e) {
            e.printStackTrace();

        }

		notification = new Notification();
		chatListener = new ChatListener(notification);
	    updateFromConfig();
	    
	    getServer().getPluginManager().registerEvents(chatListener, this);
	    
    }
    @Override
    public void onDisable() {
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	
    	boolean senderIsPlayer = (sender instanceof Player ? true : false);
    	Player pSender = (senderIsPlayer ? (Player) sender : null);
    	
    	if (cmd.getName().equalsIgnoreCase("pn")) {
    		if ((args.length < 1) || (args.length == 1 && args[0].equalsIgnoreCase("help"))) {
    		sender.sendMessage(ChatColor.AQUA + "======[" + ChatColor.WHITE + "PlayerNotify" + ChatColor.AQUA + "]======\n" 
    		+ ChatColor.WHITE + "To ping a player, type @ and their username into the chat. This is not case-sensitive.\n"
    		+ "You must type at least " + ChatColor.RED + chatListener.getMinNameLen()
			+ ChatColor.WHITE + " characters of the username in order to ping them. \n"
			+ ChatColor.GOLD + "E.g. To ping MaryAnn32, type @MaryAnn32, @MaryAnn, or @mary.\n");
    			sender.sendMessage(userCommands);
    			if (sender.hasPermission("PlayerNotify.admin.set")) sender.sendMessage(adminCommands);
    			return true;
    		}
    		
    		else if (args.length == 1) {
    			if (args[0].equalsIgnoreCase("mute")) {
    				if (sender.hasPermission("PlayerNotify.player.mute")) {
    					if (!senderIsPlayer) {
    						sender.sendMessage("You must be a player to use that command.");
    					}
    					else {
    						boolean isMuted = notification.isMutedFor(pSender.getUniqueId());
    						notification.toggleMute(pSender);
    						pSender.sendMessage("Incoming notifications are now " + ( isMuted ? "un" : "") + "muted.");
    					}
    				}
    				else {
    					sender.sendMessage(noPermission);
    				}
    				return true;
    			}
    			else if (args[0].equalsIgnoreCase("reload")) {
    				if (sender.hasPermission("PlayerNotify.admin.reload")) {
    					try {
    						reloadConfig();
    						updateFromConfig();
    						sender.sendMessage(ChatColor.GREEN + "Config reloaded.");
    					}
    					catch (Exception e) {
    						e.printStackTrace();
    						sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.DARK_RED + "Failed to reload config. (Is it formatted correctly?)");
    					}
    					return true;
    				}
    				else {
    					sender.sendMessage(noPermission);
    					log.info(sender + " attempted to perform the reload command.");
    				}
    			}
    			else {
    				if ((sender.hasPermission("PlayerNotify.player.send"))) {
    					Player receiver = getServer().getPlayer(args[0]);
    					if (receiver == null) {
    						sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.DARK_RED + "Player not found.");
    					}
    					else if (notification.isMutedFor(receiver.getUniqueId())) {
    						sender.sendMessage(ChatColor.RED + receiver.getName() + " has muted notifications.");
    					}
    					else {	
    						notification.toPlayer(receiver);
    						sender.sendMessage("Notification sent to " + receiver.getName() + ".");
    						if (sendPingNotification) {
    							receiver.sendMessage(ChatColor.GREEN + (senderIsPlayer ? sender.getName() : "The server") + " has pinged you!");
    						}
    					}
    				}
    				else {
    					sender.sendMessage(noPermission);
    				}
    				return true;
    			
    			}
    		}
    		else if (args.length == 2) {
    			if (args[0].equalsIgnoreCase("set")) {
    				if (sender.hasPermission("PlayerNotify.admin.set")) {
    					if (notification.setSound(args[1].toUpperCase(), sender)) {
    						getConfig().set("notifications.sound-effect", args[1].toUpperCase());
    						saveConfig();
    					}
    				}
    				else {
    					sender.sendMessage(noPermission);
    					log.info(sender + " attempted to perform the set [sound] command.");
    				}
    				return true;
    			}
    		}
    	}
    return false;
    }
    
	private void createConfig() {
		FileConfiguration config = getConfig();
		   config.options().header("[Configuration file for PlayerNotify by Nexamor]\n\n"
				   + "To ping a player, type the symbol plus their name (not case sensitive): @Username, or type the command /pn [username].\n"
				   + "\nconfig.yml - Configuration preferences for chat and command ping.\n"
				   + "muted.dat - Record of all players that have muted their notifications.\n");
				  
		   config.addDefault("notifications.sound-effect", "CHICKEN_EGG_POP");
		   config.addDefault("notifications.volume", 100);
		   config.addDefault("notifications.pitch", 1);

		   config.addDefault("chat.symbol", "@");
		   config.addDefault("chat.min-name-length", 3);
		   config.addDefault("chat.notify", true);
		   
		   config.options().copyDefaults(true);
		   saveConfig();
		   reloadConfig();
	}
    
    private void updateFromConfig() {
        FileConfiguration config = getConfig();
        sendPingNotification = config.getBoolean("chat.notify");
        notification.setValuesFrom(config);
        chatListener.setValuesFrom(config);
    }
}