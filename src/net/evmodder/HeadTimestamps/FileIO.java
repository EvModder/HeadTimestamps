package net.evmodder.HeadTimestamps;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Vector;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class FileIO{
	static private final String EV_DIR = "./plugins/EvFolder/";
	static private String DIR = EV_DIR;
	static final int MERGE_EV_DIR_THRESHOLD = 4;

	public static void moveDirectoryContents(File srcDir, File destDir){
		if(srcDir.isDirectory()){
			for(File file : srcDir.listFiles()){
				try{Files.move(file.toPath(), new File(destDir.getPath()+"/"+file.getName()).toPath(),
						StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);}
				catch(IOException e){e.printStackTrace();}
			}
			srcDir.delete();
		}
		else try{
			Files.move(srcDir.toPath(), new File(destDir.getPath()+"/"+srcDir.getName()).toPath(),
					StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
		}
		catch(IOException e){e.printStackTrace();}
	}

	public static Vector<String> installedEvPlugins(){
		Vector<String> evPlugins = new Vector<String>();
		for(Plugin pl : Bukkit.getServer().getPluginManager().getPlugins()){
			try{
				@SuppressWarnings("unused")
				String ver = pl.getClass().getDeclaredField("EvLib_ver").get(null).toString();
				evPlugins.add(pl.getName());
				//TODO: potentially return list of different EvLib versions being used
			}
			catch(Throwable e){}
		}
		return evPlugins;
	}

	static void verifyDir(Plugin evPl){
		final String CUSTOM_DIR = "./plugins/"+evPl.getName()+"/";
		if(!new File(EV_DIR).exists()){
			DIR = CUSTOM_DIR;
		}
		else if(new File(CUSTOM_DIR).exists()){//merge with EvFolder
			//Bukkit.getLogger().info("EvPlugins installed: "+String.join(", ", evPlugins));
			evPl.getLogger().warning("Relocating data in "+CUSTOM_DIR+", this might take a minute..");
			File evFolder = new File(EV_DIR);
			if(!evFolder.exists()) evFolder.mkdir();
			moveDirectoryContents(new File(CUSTOM_DIR), evFolder);
		}
	}

	public static YamlConfiguration loadConfig(JavaPlugin pl, String configName, InputStream defaultConfig, boolean notifyIfNew){
		if(!configName.endsWith(".yml")){
			pl.getLogger().severe("Invalid config file!");
			pl.getLogger().severe("Configuation files must end in .yml");
			return null;
		}
		File file = new File(DIR+configName);
		if(!file.exists() && defaultConfig != null){
			try{
				//Create Directory
				File dir = new File(DIR);
				if(!dir.exists())dir.mkdir();

				//Read contents of defaultConfig
				BufferedReader reader = new BufferedReader(new InputStreamReader(defaultConfig));
				String line = reader.readLine();
				StringBuilder builder = new StringBuilder(line);
				while((line = reader.readLine()) != null) builder.append('\n').append(line);
				reader.close();

				//Create new config from contents of defaultConfig
				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
				writer.write(builder.toString()); writer.close();
			}
			catch(IOException ex){
				pl.getLogger().severe(ex.getStackTrace().toString());
				pl.getLogger().severe("Unable to locate a default config!");
				pl.getLogger().severe("Could not find /config.yml in plugin's .jar");
			}
			if(notifyIfNew) pl.getLogger().info("Could not locate configuration file!");
			if(notifyIfNew) pl.getLogger().info("Generating a new one with default settings.");
		}
		return YamlConfiguration.loadConfiguration(file);
	}

	public static boolean saveConfig(String configName, FileConfiguration config){
		try{
			if(!new File(DIR).exists()) new File(DIR).mkdir();
			config.save(DIR+configName);
		}
		catch(IOException ex){ex.printStackTrace(); return false;}
		return true;
	}
}