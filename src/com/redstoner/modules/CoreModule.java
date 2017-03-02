package com.redstoner.modules;

import com.redstoner.annotations.Version;

/** This class shall be used for "CoreModules", which are acting on a lower level than modules and are also exempted from being disabled or reloaded on the go.</br>
 * Please note that CoreModules will not be known to the ModuleLoader itself!</br>
 * Examples are the ModuleLoader and the Debugger.
 * 
 * @author Pepich */
@Version(major = 2, minor = 0, revision = 0, compatible = -1)
public interface CoreModule extends Module
{
	/** Core modules don't need to be enabled. */
	@Override
	public default boolean onEnable()
	{
		return true;
	}
	
	/** Core modules don't need to be disabled. */
	@Override
	public default void onDisable()
	{}
}
