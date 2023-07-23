package net.evmodder.HeadTimestamps;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import net.evmodder.DropHeads.DropHeads;
import net.evmodder.DropHeads.events.EntityBeheadEvent;

public final class HeadTimestamps extends JavaPlugin implements Listener{
	private FileConfiguration config;
	private DropHeads dropheadsPlugin = null;
	private DateTimeFormatter defaultFormatter = null;
	private HashMap<EntityType, DateTimeFormatter> formatters = null;

	private DropHeads getDropHeadsPlugin(){
		if(dropheadsPlugin == null) dropheadsPlugin = (DropHeads)getServer().getPluginManager().getPlugin("DropHeads");
		return dropheadsPlugin;
	}

	@Override public FileConfiguration getConfig(){return config;}
	@Override public void saveConfig(){
		if(config != null && !FileIO.saveConfig("config-"+getName()+".yml", config)){
			getLogger().severe("Error while saving plugin configuration file!");
		}
	}
	@Override public void reloadConfig(){
		InputStream defaultConfig = getClass().getResourceAsStream("/config.yml");
		if(defaultConfig != null){
			FileIO.verifyDir(getDropHeadsPlugin());
			config = FileIO.loadConfig(this, "config-"+getName()+".yml", defaultConfig, /*notifyIfNew=*/true);
		}
	}

	@Override public void onEnable(){
		reloadConfig();
		formatters = new HashMap<>();
		Locale locale = Locale.forLanguageTag(config.getString("locale", "us"));
		
		ConfigurationSection datetimeFormats = config.getConfigurationSection("date-time-format");
		if(datetimeFormats != null){
			for(String entityName : datetimeFormats.getKeys(/*deep=*/false)){
				try{
					final EntityType eType = EntityType.valueOf(entityName.toUpperCase().replace("DEFAULT", "UNKNOWN"));
					final String formatStr = ChatColor.translateAlternateColorCodes('&', datetimeFormats.getString(entityName));
					formatters.put(eType, DateTimeFormatter.ofPattern(formatStr, locale));
				}
				catch(IllegalArgumentException ex){getLogger().severe("Unknown EntityType in 'date-time-format': "+entityName);}
			}
			defaultFormatter = formatters.get(EntityType.UNKNOWN);
		}
		if(!formatters.isEmpty()) getServer().getPluginManager().registerEvents(new Listener(){
			@EventHandler(ignoreCancelled = true)
			public void onEntityBehead(EntityBeheadEvent evt){
				final DateTimeFormatter formatter = formatters.getOrDefault(evt.getEntityType(), defaultFormatter);
				if(formatter != null){
					final ItemMeta meta = evt.getHeadItem().getItemMeta();
					final List<String> lore = meta.getLore();
					lore.add(formatter.format(LocalDateTime.now()));
					meta.setLore(lore);
					evt.getHeadItem().setItemMeta(meta);
				}
			}
		}, this);
	}
}