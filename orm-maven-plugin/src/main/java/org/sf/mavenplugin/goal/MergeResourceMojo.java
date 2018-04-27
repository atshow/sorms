package org.sf.mavenplugin.goal;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Goal which touches a timestamp file.
 * 
 * @goal toedl
 * @phase generate-sources
 * */
public class MergeResourceMojo extends AbstractMojo {
	private Logger log = LoggerFactory.getLogger(this.getClass());
	/**
	 * @parameter property="${basedir}"
	 */
	private String path;

	/**
	 * @parameter property="${sdl.path}"
	 */
	private String sdlPath;
	
	/**
	 * @parameter property="${edl.module.for.sdl}"
	 */	
	private String edlModuleForSdl;

	public void execute() throws MojoExecutionException {
		long time = System.currentTimeMillis();

		File sdlDir=new File(sdlPath);
		
	}
}
