package com.redstoner.modules.saylol;

import java.io.File;
import java.util.Random;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;

import com.nemez.cmdmgr.Command;
import com.redstoner.annotations.Version;
import com.redstoner.misc.BroadcastFilter;
import com.redstoner.misc.JsonManager;
import com.redstoner.misc.Main;
import com.redstoner.misc.Utils;
import com.redstoner.modules.Module;

@Version(major = 1, minor = 0, revision = 3, compatible = 1)
public class Saylol implements Module
{
	private long lastLol = 0;
	private boolean enabled = false;
	private File lolLocation = new File(Main.plugin.getDataFolder(), "lol.json");
	private JSONArray lols;
	
	@Override
	public void onEnable()
	{
		lols = JsonManager.getArray(lolLocation);
		if (lols == null)
			lols = new JSONArray();
		enabled = true;
	}
	
	@Override
	public void onDisable()
	{
		saveLols();
		enabled = false;
	}
	
	@Override
	public boolean enabled()
	{
		return enabled;
	}
	
	@SuppressWarnings("unchecked")
	@Command(hook = "addlol")
	public boolean addLol(CommandSender sender, String text)
	{
		if (lols.contains(text))
			Utils.sendErrorMessage(sender, null, "This lol already exists!");
		else
		{
			Utils.sendMessage(sender, null, "Successfully added a new lol!");
			lols.add("&e" + text);
			saveLols();
		}
		return true;
	}
	
	@Command(hook = "dellol")
	public boolean delLol(CommandSender sender, int id)
	{
		if (id < 0 || id >= lols.size())
		{
			Utils.sendErrorMessage(sender, null, "The ID must be at least 0 and at most " + lols.size());
			return true;
		}
		Utils.sendMessage(sender, null, "Successfully deleted the lol: " + lols.remove(id), '&');
		saveLols();
		return true;
	}
	
	@SuppressWarnings("unchecked")
	@Command(hook = "setlol")
	public boolean setLol(CommandSender sender, int id, String text)
	{
		if (id < 0 || id >= lols.size())
		{
			Utils.sendErrorMessage(sender, null, "The ID must be at least 0 and at most " + lols.size());
			return true;
		}
		Utils.sendMessage(sender, null, "Successfully changed the lol: &e" + lols.get(id) + " &7to: &e" + text, '&');
		lols.set(id, text);
		saveLols();
		return true;
	}
	
	@Command(hook = "lolid")
	public boolean lolId(CommandSender sender, int id)
	{
		long time = System.currentTimeMillis();
		if (time - lastLol < 15000)
		{
			Utils.sendErrorMessage(sender, null,
					"You can't use saylol for another " + (14 - (int) Math.ceil((time - lastLol) / 1000)) + "s.");
			return true;
		}
		if (id < 0 || id >= lols.size())
		{
			Utils.sendErrorMessage(sender, null, "The ID must be at least 0 and at most " + lols.size());
			return true;
		}
		String name;
		if (sender instanceof Player)
			name = ((Player) sender).getDisplayName();
		else
			name = "&9" + sender.getName();
		Utils.broadcast("&8[&blol&8] ", name + "&8: &e" + lols.get(id), new BroadcastFilter()
		{
			@Override
			public boolean sendTo(CommandSender recipient)
			{
				return recipient.hasPermission("utils.lol.see");
			}
		}, '&');
		lastLol = time;
		return true;
	}
	
	@Command(hook = "saylol")
	public boolean saylol(CommandSender sender)
	{
		long time = System.currentTimeMillis();
		int timeout = 15000;
		if (time - lastLol < timeout)
		{
			Utils.sendErrorMessage(sender, null,
					"You can't use saylol for another " + (timeout - (time - lastLol)) / 1000 + "s.");
			return true;
		}
		String name;
		if (sender instanceof Player)
			name = ((Player) sender).getDisplayName();
		else
			name = "&9" + sender.getName();
		Random random = new Random();
		int id = random.nextInt(lols.size());
		Utils.broadcast("&8[&blol&8] ", name + "&8: &e" + lols.get(id), new BroadcastFilter()
		{
			@Override
			public boolean sendTo(CommandSender recipient)
			{
				return recipient.hasPermission("utils.lol.see");
			}
		}, '&');
		lastLol = time;
		return true;
	}
	
	@Command(hook = "listlols")
	public boolean listLols(CommandSender sender, int page)
	{
		page = page - 1;
		int start = page * 10;
		int end = start + 10;
		int pages = (int) Math.ceil(lols.size() / 10d);
		if (start < 0)
		{
			Utils.sendErrorMessage(sender, null, "Page number too small, must be at least 0!");
			return true;
		}
		if (start > lols.size())
		{
			Utils.sendErrorMessage(sender, null, "Page number too big, must be at most " + pages + "!");
			return true;
		}
		Utils.sendModuleHeader(sender);
		Utils.sendMessage(sender, "", "&ePage " + (page + 1) + "/" + pages + ":", '&');
		for (int i = start; i < end && i < lols.size(); i++)
			Utils.sendMessage(sender, "", "&a" + i + "&8: &e" + lols.get(i), '&');
		return true;
	}
	
	@Command(hook = "listlolsdef")
	public boolean listLolsDefault(CommandSender sender)
	{
		return listLols(sender, 1);
	}
	
	@Command(hook = "searchlol")
	public boolean search(CommandSender sender, boolean insensitive, String text)
	{
		Utils.sendModuleHeader(sender);
		boolean found = false;
		if (insensitive)
		{
			text = text.toLowerCase();
			for (int i = 0; i < lols.size(); i++)
			{
				if (((String) lols.get(i)).toLowerCase().contains(text))
				{
					Utils.sendMessage(sender, "", "&a" + i + "&8: &e" + lols.get(i), '&');
					found = true;
				}
			}
		}
		else
		{
			for (int i = 0; i < lols.size(); i++)
			{
				if (((String) lols.get(i)).contains(text))
				{
					Utils.sendMessage(sender, "", "&a" + i + "&8: &e" + lols.get(i), '&');
					found = true;
				}
			}
		}
		if (!found)
		{
			Utils.sendMessage(sender, "", "&cCouldn't find any matching lols.", '&');
		}
		return true;
	}
	
	@Command(hook = "matchlol")
	public boolean match(CommandSender sender, boolean insensitive, String regex)
	{
		Utils.sendModuleHeader(sender);
		boolean found = false;
		if (insensitive)
		{
			regex = regex.toLowerCase();
			for (int i = 0; i < lols.size(); i++)
			{
				if (((String) lols.get(i)).toLowerCase().matches(regex))
				{
					Utils.sendMessage(sender, "", "&a" + i + ": " + lols.get(i), '&');
					found = true;
				}
			}
		}
		else
		{
			for (int i = 0; i < lols.size(); i++)
			{
				if (((String) lols.get(i)).matches(regex))
				{
					Utils.sendMessage(sender, "", "&a" + i + ": " + lols.get(i), '&');
					found = true;
				}
			}
		}
		if (!found)
		{
			Utils.sendMessage(sender, "", "&cCouldn't find any matching lols.", '&');
		}
		return true;
	}
	
	public void saveLols()
	{
		JsonManager.save(lols, lolLocation);
	}
	
	// @noformat
	@Override
	public String getCommandString()
	{
		return "command lol {\n" + 
				"    add [string:text...] {\n" + 
				"        help Lols a text.;\n" + 
				"        run addlol text;\n" + 
				"        perm utils.lol.admin;\n" + 
				"    }\n" + 
				"    del [int:id] {\n" + 
				"        help Unlols a lol.;\n" + 
				"        run dellol id;\n" + 
				"        perm utils.lol.admin;\n" + 
				"    }\n" + 
				"    set [int:id] [string:text...] {\n" + 
				"        help Relols a lol.;\n" + 
				"        run setlol id text;\n" + 
				"        perm utils.lol.admin;\n" + 
				"    }\n" + 
				"    id [int:id] {\n" + 
				"        help Lols specifically.;\n" + 
				"        run lolid id;\n" + 
				"        perm utils.lol.id;\n" + 
				"    }\n" + 
				"    list [int:page] {\n" + 
				"        help Shows lols.;\n" + 
				"        run listlols page;\n" + 
				"        perm utils.lol.list;\n" + 
				"    }\n" + 
				"    list {\n" + 
				"        help Shows lols.;\n" + 
				"        run listlolsdef;\n" + 
				"        perm utils.lol.list;\n" + 
				"    }\n" + 
				"    search [flag:-i] [string:text...] {\n" + 
				"        help Search lols.;\n" + 
				"        run searchlol -i text;\n" + 
				"        perm utils.lol.search;\n" + 
				"    }\n" + 
				"    match [flag:-i] [string:regex...] {\n" + 
				"        help Search lols. But better.;\n" + 
				"        run matchlol -i regex;\n" + 
				"        perm utils.lol.match;\n" + 
				"    }\n" + 
				"    [empty] {\n" + 
				"        help Lols.;\n" + 
				"        run saylol;\n" + 
				"        perm utils.lol;\n" + 
				"    }\n" + 
				"}";
	}
	// @format
}
