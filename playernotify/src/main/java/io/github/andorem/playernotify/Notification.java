package io.github.andorem.playernotify;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;



public class Notification {
	ArrayList<UUID> hasMuted;
	Sound sound;
	int pitch;
	int volume;
	String mutePath = PlayerNotify.DATA_FOLDER + File.separator + "muted.dat";
	
	public Notification() {
		File file = new File(mutePath);
		if (file.exists()) {
			hasMuted = (ArrayList<UUID>) load(mutePath);
		} 
		else {
			hasMuted = new ArrayList<UUID>();
			save(hasMuted, mutePath);
		}
	}

	void toPlayer(Player receiver) {
		if ((receiver != null) && !hasMuted.contains(receiver.getUniqueId())) {
			Location receiverLocation = receiver.getLocation();
			receiver.playSound(receiverLocation, sound, volume, pitch);
		}
	}
	
	void toggleMute(Player player) {
		UUID playerUUID = player.getUniqueId();
		if (hasMuted.contains(playerUUID)) {
			hasMuted.remove(playerUUID);
		}
		else {
			hasMuted.add(playerUUID);
		}
		save(hasMuted, mutePath);
	}
	
	boolean isMutedFor(UUID playerUUID) {
		return hasMuted.contains(playerUUID);
	}
	
	boolean setSound(String soundName, CommandSender sender) {
		Sound newSound;
		try {
			newSound = Sound.valueOf(soundName);
		}
		catch (Exception e) {
			sender.sendMessage(ChatColor.RED + "That is not a valid sound name!");
			return false;
		}
		sound = newSound;
		sender.sendMessage("Notification sound set to " + ChatColor.GREEN + soundName);
		return true;
	}
	
	Sound getSound() {
		return sound;
	}
	
	Object load(String path) {
		try {
    		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
    		Object result = ois.readObject();
    		ois.close();
    		return result;
    	} catch (Exception e) {
    		e. printStackTrace();
    		return null;
    	}
	}
	
	void save(Object object, String path) {
		try {
		   ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
		   oos.writeObject(object);
		   oos.flush();
		   oos.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}   
	}
	
	void setValuesFrom(FileConfiguration config) {
		sound = Sound.valueOf(config.getString("notifications.sound-effect").toUpperCase());
        volume = config.getInt("notifications.volume");
        pitch = config.getInt("notifications.pitch");
	}
}
