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
	//private DateTimeFormatter defaultFormatter = null;
	private HashMap<String, DateTimeFormatter> dateFormats = null;
	private HashMap<EntityType, String> loreFormats = null;

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
			// Save our config in same folder as DropHeads' config
			FileIO.verifyDir(getDropHeadsPlugin());
			config = FileIO.loadConfig(this, "config-"+getName()+".yml", defaultConfig, /*notifyIfNew=*/true);
		}
	}

	@Override public void onEnable(){
		reloadConfig();
		loreFormats = new HashMap<>();
		ConfigurationSection loreFormatSection = config.getConfigurationSection("lore-format");
		if(loreFormatSection != null){
			for(String entityName : loreFormatSection.getKeys(/*deep=*/false)){
				try{
					final EntityType eType = EntityType.valueOf(entityName.toUpperCase().replace("DEFAULT", "UNKNOWN"));
					final String formatStr = ChatColor.translateAlternateColorCodes('&', loreFormatSection.getString(entityName));
					loreFormats.put(eType, formatStr);
				}
				catch(IllegalArgumentException ex){getLogger().severe("Unknown EntityType in 'lore-format': "+entityName);}
			}
		}
		if(!loreFormats.isEmpty()){
			dateFormats = new HashMap<>();
			getServer().getPluginManager().registerEvents(new Listener(){
				// Substitute a tag
				String substituteTag(final String tag, EntityBeheadEvent evt){
					switch(tag){
						case "TIMESTAMP":
							return ""+System.currentTimeMillis();
						case "VICTIM":
							return evt.getVictim().getName();
						case "KILLER":
							return evt.getKiller().getName();
						default:
							DateTimeFormatter f = dateFormats.get(tag);
							if(f == null){
								final Locale locale = Locale.forLanguageTag(config.getString("locale", "us"));
								f = DateTimeFormatter.ofPattern(tag, locale);
								dateFormats.put(tag, f);
							}
							return f.format(LocalDateTime.now());
					}
				}
				@EventHandler(ignoreCancelled = true)
				public void onEntityBehead(EntityBeheadEvent evt){
					final String formatter = loreFormats.getOrDefault(evt.getEntityType(), loreFormats.get(EntityType.UNKNOWN));
					if(formatter != null){
						// Parse formatter
						final StringBuilder result = new StringBuilder();
						int i=formatter.indexOf("${"),j=formatter.indexOf('}', i),s=0;
						while(i != -1 && j != -1){
							result.append(formatter.substring(s, i));
							result.append(substituteTag(formatter.substring(i+2, j), evt));
							s = j+1;
							i = formatter.indexOf("${", j);
							j = formatter.indexOf('}', i);
						}
						result.append(formatter.substring(s));

						// Set lore
						final ItemMeta meta = evt.getHeadItem().getItemMeta();
						final List<String> lore = meta.getLore();
						for(String line : result.toString().split("\\n")) lore.add(line);
						meta.setLore(lore);
						evt.getHeadItem().setItemMeta(meta);
					}
				}
			}, this);
		}
	}
}