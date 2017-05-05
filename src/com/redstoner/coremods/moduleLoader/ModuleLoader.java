package com.redstoner.coremods.moduleLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import com.nemez.cmdmgr.Command;
import com.nemez.cmdmgr.Command.AsyncType;
import com.nemez.cmdmgr.CommandManager;
import com.redstoner.annotations.AutoRegisterListener;
import com.redstoner.annotations.Debugable;
import com.redstoner.annotations.Version;
import com.redstoner.coremods.debugger.Debugger;
import com.redstoner.misc.Main;
import com.redstoner.misc.Utils;
import com.redstoner.misc.VersionHelper;
import com.redstoner.modules.CoreModule;
import com.redstoner.modules.Module;

import net.minecraft.server.v1_11_R1.MinecraftServer;

/** The module loader, mother of all modules. Responsible for loading and taking care of all modules.
 * 
 * @author Pepich */
@Version(major = 3, minor = 2, revision = 2, compatible = 2)
public final class ModuleLoader implements CoreModule
{
	private static ModuleLoader instance;
	private static final HashMap<Module, Boolean> modules = new HashMap<Module, Boolean>();
	private static URL[] urls;
	private static URLClassLoader mainLoader;
	private static HashMap<Module, URLClassLoader> loaders = new HashMap<Module, URLClassLoader>();
	private static File configFile;
	private static FileConfiguration config;
	
	private ModuleLoader()
	{
		try
		{
			config = Main.plugin.getConfig();
			configFile = new File(Main.plugin.getDataFolder(), "config.yml");
			urls = new URL[] {(new File(Main.plugin.getDataFolder(), "classes")).toURI().toURL()};
			mainLoader = new URLClassLoader(urls, this.getClass().getClassLoader());
		}
		catch (MalformedURLException e)
		{
			System.out.println("Sumtin is wong with ya filesüstem m8. Fix eeeet or I won't werk!");
		}
	}
	
	public static void init()
	{
		if (instance == null)
			instance = new ModuleLoader();
		CommandManager.registerCommand(ModuleLoader.class.getResourceAsStream("ModuleLoader.cmd"), instance,
				Main.plugin);
	}
	
	public static final void loadFromConfig()
	{
		try
		{
			if (!configFile.exists())
			{
				configFile.getParentFile().mkdirs();
				configFile.createNewFile();
			}
			config.load(configFile);
		}
		catch (FileNotFoundException e)
		{}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (InvalidConfigurationException e)
		{
			configFile.delete();
			try
			{
				configFile.createNewFile();
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
			Utils.error("Invalid config file! Creating new, blank file!");
		}
		List<String> coremods = config.getStringList("coremods");
		if (coremods == null || coremods.isEmpty())
		{
			config.set("coremods", new String[] {"# Add the coremodules here!"});
			Main.plugin.saveConfig();
			try
			{
				config.save(configFile);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		List<String> autoload = config.getStringList("autoload");
		if (autoload == null || autoload.isEmpty())
		{
			config.set("autoload", new String[] {"# Add the modules here!"});
			Main.plugin.saveConfig();
			try
			{
				config.save(configFile);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		for (String s : coremods)
			if (!s.startsWith("#"))
				ModuleLoader.addDynamicModule(s);
		enableModules();
		for (String s : autoload)
			if (!s.startsWith("#"))
				ModuleLoader.addDynamicModule(s);
	}
	
	/** This method will add a module to the module list, without enabling it.</br>
	 * This method is deprecated, use addDynamicModule(String name) instead. When using this method, dynamic reloading of the module will not be supported.
	 * 
	 * @param clazz The class of the module to be added. */
	@Debugable
	@Deprecated
	public static final void addModule(Class<? extends Module> clazz)
	{
		Debugger.notifyMethod(clazz);
		try
		{
			Module module = clazz.newInstance();
			modules.put(module, false);
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			Utils.error("Could not add " + clazz.getName() + " to the list, constructor not accessible.");
		}
	}
	
	@Debugable
	private static final void addLoadedModule(Module m)
	{
		Debugger.notifyMethod(m);
		if (modules.containsKey(m))
			if (modules.get(m))
			{
				Utils.error(
						"Module m was already loaded and enabled. Disable the module before attempting to reload it.");
				return;
			}
		modules.put(m, false);
	}
	
	/** Call this to enable all not-yet enabled modules that are known to the loader. */
	@Debugable
	public static final void enableModules()
	{
		Debugger.notifyMethod();
		for (Module module : modules.keySet())
		{
			if (modules.get(module))
				continue;
			enableLoadedModule(module);
		}
	}
	
	/** This method enables a specific module. If no module with that name is known to the loader yet it will be added to the list.</br>
	 * This method is deprecated, use enableDynamicModule instead. When using this method, dynamic reloading of the module will not be supported.
	 * 
	 * @param clazz The class of the module to be enabled.
	 * @return true, when the module was successfully enabled. */
	@Debugable
	@Deprecated
	public static final boolean enableModule(Class<? extends Module> clazz)
	{
		Debugger.notifyMethod(clazz);
		for (Module module : modules.keySet())
		{
			if (module.getClass().equals(clazz))
			{
				if (modules.get(module))
				{
					Utils.info("Module was already enabled! Ignoring module.!");
					return true;
				}
				if (module.onEnable())
				{
					if (module.getClass().isAnnotationPresent(AutoRegisterListener.class)
							&& (module instanceof Listener))
					{
						Bukkit.getPluginManager().registerEvents((Listener) module, Main.plugin);
					}
					Utils.info("Enabled module " + module.getClass().getName());
					Utils.info("Loaded module " + module.getClass().getName());
					modules.put(module, true);
					return true;
				}
				else
				{
					Utils.error("Failed to enable module " + module.getClass().getName());
					return false;
				}
			}
		}
		try
		{
			Module m = clazz.newInstance();
			modules.put(m, false);
			if (m.onEnable())
			{
				if (m.getClass().isAnnotationPresent(AutoRegisterListener.class) && (m instanceof Listener))
				{
					Bukkit.getPluginManager().registerEvents((Listener) m, Main.plugin);
				}
				Utils.info("Loaded and enabled module " + m.getClass().getName());
				Utils.info("Loaded module " + m.getClass().getName());
				return true;
			}
			else
			{
				Utils.error("Failed to enable module " + m.getClass().getName());
				return false;
			}
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			Utils.error("Could not add " + clazz.getName() + " to the list, constructor not accessible.");
			return false;
		}
	}
	
	@SuppressWarnings("deprecation")
	private static final void enableLoadedModule(Module module)
	{
		try
		{
			if (module.onEnable())
			{
				if (VersionHelper.isCompatible(VersionHelper.create(2, 0, 0, -1), module.getClass()))
					CommandManager.registerCommand(module.getCommandString(), module, Main.plugin);
				modules.put(module, true);
				if (VersionHelper.isCompatible(VersionHelper.create(3, 0, 0, 3), module.getClass()))
				{
					module.postEnable();
				}
				Utils.info("Loaded module " + module.getClass().getName());
				if (module.getClass().isAnnotationPresent(AutoRegisterListener.class) && (module instanceof Listener))
				{
					Bukkit.getPluginManager().registerEvents((Listener) module, Main.plugin);
				}
			}
			else
				Utils.error("Failed to load module " + module.getClass().getName());
		}
		catch (Exception e)
		{
			Utils.error("Failed to load module " + module.getClass().getName());
			e.printStackTrace();
		}
	}
	
	/** This method lists all modules to the specified CommandSender. The modules will be color coded correspondingly to their enabled status.
	 * 
	 * @param sender The person to send the info to, usually the issuer of the command or the console sender.
	 * @return true. */
	@Command(hook = "list", async = AsyncType.ALWAYS)
	public boolean listModulesCommand(CommandSender sender)
	{
		Utils.sendModuleHeader(sender);
		StringBuilder sb = new StringBuilder("Modules:\n");
		for (Module module : modules.keySet())
		{
			String[] classPath = module.getClass().getName().split("\\.");
			String classname = classPath[classPath.length - 1];
			sb.append(modules.get(module) ? "&a" : "&c");
			sb.append(classname);
			sb.append(", ");
		}
		sb.delete(sb.length() - 2, sb.length());
		Utils.sendMessage(sender, " §e", sb.toString(), '&');
		Utils.sendMessage(sender, " §7", "For more detailed information, consult the debugger.");
		return true;
	}
	
	public static void disableModules()
	{
		for (Module module : modules.keySet())
		{
			disableModule(module);
		}
	}
	
	public static void disableModule(Module module)
	{
		if (modules.get(module))
		{
			module.onDisable();
			if (module.getClass().isAnnotationPresent(AutoRegisterListener.class) && (module instanceof Listener))
			{
				HandlerList.unregisterAll((Listener) module);
			}
			String[] commands = getAllHooks(module).toArray(new String[] {});
			CommandManager.unregisterAll(commands);
		}
	}
	
	private static ArrayList<String> getAllHooks(Module module)
	{
		ArrayList<String> commands = new ArrayList<String>();
		for (Method m : module.getClass().getMethods())
		{
			Command cmd = m.getDeclaredAnnotation(Command.class);
			if (cmd == null)
				continue;
			commands.add(cmd.hook());
		}
		return commands;
	}
	
	@Command(hook = "load")
	public boolean loadModule(CommandSender sender, String name)
	{
		addDynamicModule(name);
		return true;
	}
	
	@Command(hook = "unload")
	public boolean unloadModule(CommandSender sender, String name)
	{
		removeDynamicModule(name);
		return true;
	}
	
	public static final void addDynamicModule(String name)
	{
		Object[] status = getServerStatus();
		for (Module m : modules.keySet())
		{
			if (m.getClass().getName().equals(name))
			{
				Utils.info(
						"Found existing module, attempting override. WARNING! This operation will halt the main thread until it is completed.");
				Utils.info("Current server status:");
				Utils.info("Current system time: " + status[0]);
				Utils.info("Current tick: " + status[1]);
				Utils.info("Last TPS: " + status[2]);
				Utils.info("Entity count: " + status[3]);
				Utils.info("Player count: " + status[4]);
				Utils.info("Attempting to load new class definition before disabling and removing the old module:");
				boolean differs = false;
				Utils.info("Old class definition: Class@" + m.getClass().hashCode());
				ClassLoader delegateParent = mainLoader.getParent();
				Class<?> newClass = null;
				URLClassLoader cl = new URLClassLoader(urls, delegateParent);
				try
				{
					newClass = cl.loadClass(m.getClass().getName());
					Utils.info("Found new class definition: Class@" + newClass.hashCode());
					differs = m.getClass() != newClass;
				}
				catch (ClassNotFoundException e)
				{
					Utils.error("Could not find a class definition, aborting now!");
					e.printStackTrace();
					try
					{
						cl.close();
					}
					catch (IOException e1)
					{
						e1.printStackTrace();
					}
					return;
				}
				if (!differs)
				{
					Utils.warn("New class definition equals old definition, are you sure you did everything right?");
					Utils.info("Aborting now...");
					try
					{
						cl.close();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
					return;
				}
				Utils.info("Found new class definition, attempting to instantiate:");
				Module module = null;
				try
				{
					module = (Module) newClass.newInstance();
				}
				catch (InstantiationException | IllegalAccessException e)
				{
					Utils.error("Could not instantiate the module, aborting!");
					e.printStackTrace();
					try
					{
						cl.close();
					}
					catch (IOException e1)
					{
						e1.printStackTrace();
					}
					return;
				}
				Utils.info("Instantiated new class definition, checking versions:");
				Version oldVersion = m.getClass().getAnnotation(Version.class);
				Utils.info("Current version: " + VersionHelper.getString(oldVersion));
				Version newVersion = module.getClass().getAnnotation(Version.class);
				Utils.info("Version of remote class: " + VersionHelper.getString(newVersion));
				if (oldVersion.equals(newVersion))
				{
					Utils.error("Detected equal module versions, aborting now...");
					try
					{
						cl.close();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
					return;
				}
				Utils.info("Versions differ, disabling old module:");
				disableModule(m);
				Utils.info("Disabled module, overriding the implementation:");
				modules.remove(m);
				try
				{
					if (loaders.containsKey(m))
						loaders.remove(m).close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				modules.put(module, false);
				loaders.put(module, cl);
				Utils.info("Successfully updated class definition. Enabling new implementation:");
				enableLoadedModule(module);
				Object[] newStatus = getServerStatus();
				Utils.info("Task complete! Took " + ((long) newStatus[0] - (long) status[0]) + "ms to finish!");
				Utils.info("Current server status:");
				Utils.info("Current system time: " + newStatus[0]);
				Utils.info("Current tick: " + newStatus[1]);
				Utils.info("Last TPS: " + newStatus[2]);
				Utils.info("Entity count: " + newStatus[3]);
				Utils.info("Player count: " + newStatus[4]);
				return;
			}
		}
		try
		{
			Class<?> clazz = mainLoader.loadClass(name);
			Module module = (Module) clazz.newInstance();
			addLoadedModule(module);
			enableLoadedModule(module);
		}
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException e)
		{
			if (!name.startsWith("com.redstoner.modules."))
			{
				Utils.warn(
						"Couldn't find class definition, suspecting missing path. Autocompleting path, trying again.");
				addDynamicModule("com.redstoner.modules." + name);
			}
			else
				e.printStackTrace();
		}
	}
	
	public static final void removeDynamicModule(String name)
	{
		Object[] status = getServerStatus();
		for (Module m : modules.keySet())
		{
			if (m.getClass().getName().equals(name))
			{
				Utils.info(
						"Found existing module, attempting unload. WARNING! This operation will halt the main thread until it is completed.");
				Utils.info("Current server status:");
				Utils.info("Current system time: " + status[0]);
				Utils.info("Current tick: " + status[1]);
				Utils.info("Last TPS: " + status[2]);
				Utils.info("Entity count: " + status[3]);
				Utils.info("Player count: " + status[4]);
				Utils.info("Attempting to disable module properly:");
				disableModule(m);
				modules.remove(m);
				Utils.info("Disabled module, overriding the implementation:");
				Object[] newStatus = getServerStatus();
				Utils.info("Task complete! Took " + ((long) newStatus[0] - (long) status[0]) + "ms to finish!");
				Utils.info("Current server status:");
				Utils.info("Current system time: " + newStatus[0]);
				Utils.info("Current tick: " + newStatus[1]);
				Utils.info("Last TPS: " + newStatus[2]);
				Utils.info("Entity count: " + newStatus[3]);
				Utils.info("Player count: " + newStatus[4]);
				return;
			}
		}
		if (!name.startsWith("com.redstoner.modules."))
		{
			Utils.warn("Couldn't find class definition, suspecting missing path. Autocompleting path, trying again.");
			removeDynamicModule("com.redstoner.modules." + name);
		}
		else
			Utils.error("Couldn't find module! Couldn't ");
	}
	
	@SuppressWarnings("deprecation")
	private static final Object[] getServerStatus()
	{
		final Object[] status = new Object[5];
		status[0] = System.currentTimeMillis();
		status[1] = MinecraftServer.currentTick;
		status[2] = MinecraftServer.getServer().recentTps[0];
		int i = 0;
		for (World w : Bukkit.getWorlds())
		{
			i += w.getEntities().size();
		}
		status[3] = i;
		status[4] = Bukkit.getOnlinePlayers().size();
		return status;
	}
	
	/** Finds a module by name for other modules to reference it.
	 * 
	 * @param name the name of the module. Use the full path if you are not sure about the module's SimpleClassName being unique.
	 * @return the instance of the module or @null it none could be found */
	public static Module getModule(String name)
	{
		for (Module m : modules.keySet())
			if (m.getClass().getSimpleName().equals(name) || m.getClass().getName().equals(name))
				return m;
		return null;
	}
}
