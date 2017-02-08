package com.redstoner.misc;

import org.bukkit.plugin.java.JavaPlugin;

import com.redstoner.annotations.Version;
import com.redstoner.coremods.debugger.Debugger;
import com.redstoner.coremods.moduleLoader.ModuleLoader;
import com.redstoner.misc.mysql.MysqlHandler;
import com.redstoner.modules.abot.Abot;
import com.redstoner.modules.adminchat.Adminchat;
import com.redstoner.modules.adminnotes.AdminNotes;
import com.redstoner.modules.chatalias.Chatalias;
import com.redstoner.modules.chatgroups.Chatgroups;
import com.redstoner.modules.check.Check;
import com.redstoner.modules.cycle.Cycle;
import com.redstoner.modules.damnspam.DamnSpam;
import com.redstoner.modules.illumination.Illumination;
import com.redstoner.modules.imout.Imout;
import com.redstoner.modules.lagchunks.LagChunks;
import com.redstoner.modules.loginsecurity.LoginSecurity;
import com.redstoner.modules.mentio.Mentio;
import com.redstoner.modules.misc.Misc;
import com.redstoner.modules.motd.Motd;
import com.redstoner.modules.nametags.Nametags;
import com.redstoner.modules.pmtoggle.Pmtoggle;
import com.redstoner.modules.reports.Reports;
import com.redstoner.modules.saylol.Saylol;
import com.redstoner.modules.scriptutils.Scriptutils;
import com.redstoner.modules.skullclick.SkullClick;
import com.redstoner.modules.tag.Tag;
import com.redstoner.modules.vanish.Vanish;
import com.redstoner.modules.warn.Warn;
import com.redstoner.modules.webtoken.WebToken;

/** Main class. Duh.
 * 
 * @author Pepich */
@Version(major = 1, minor = 4, revision = 1, compatible = -1)
public class Main extends JavaPlugin
{
	public static JavaPlugin plugin;
	
	@Override
	public void onEnable()
	{
		plugin = this;
		Debugger.init();
		ModuleLoader.init();
		MysqlHandler.init();
		ModuleLoader.addModule(Abot.class);
		ModuleLoader.addModule(Adminchat.class);
		ModuleLoader.addModule(AdminNotes.class);
		// TODO: ModuleLoader.addModule(BlockplaceMods.class);
		// TODO: ModuleLoader.addModule(Calc.class);
		ModuleLoader.addModule(Chatalias.class);
		ModuleLoader.addModule(Cycle.class);
		ModuleLoader.addModule(Chatgroups.class);
		ModuleLoader.addModule(Check.class);
		ModuleLoader.addModule(DamnSpam.class);
		// TODO: ModuleLoader.addModule(Friends.class);
		ModuleLoader.addModule(Illumination.class);
		// TODO: ModuleLoader.addModule(Imbusy.class);
		ModuleLoader.addModule(Imout.class);
		ModuleLoader.addModule(LagChunks.class);
		ModuleLoader.addModule(LoginSecurity.class);
		ModuleLoader.addModule(Mentio.class);
		ModuleLoader.addModule(Misc.class);
		ModuleLoader.addModule(Motd.class);
		ModuleLoader.addModule(Nametags.class);
		ModuleLoader.addModule(Pmtoggle.class);
		ModuleLoader.addModule(Reports.class);
		ModuleLoader.addModule(Saylol.class);
		ModuleLoader.addModule(Scriptutils.class);
		// TODO: ModuleLoader.addModule(Serversigns.class);
		// TODO: ModuleLoader.addModule(Signalstrength.class);
		ModuleLoader.addModule(SkullClick.class);
		ModuleLoader.addModule(Tag.class);
		ModuleLoader.addModule(Vanish.class);
		ModuleLoader.addModule(Warn.class);
		ModuleLoader.addModule(WebToken.class);
		// And enable them
		ModuleLoader.enableModules();
	}
	
	@Override
	public void onDisable()
	{
		ModuleLoader.disableModules();
	}
}
