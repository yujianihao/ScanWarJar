package main.java;

public class JarVersion{
	
	private String jarName;
	
	private String jarVersion;
	
	private String jarBuildTime;

	public String getJarName() {
		return jarName;
	}

	public void setJarName(String jarName) {
		this.jarName = jarName;
	}

	public String getJarVersion() {
		return jarVersion;
	}

	public void setJarVersion(String jarVersion) {
		this.jarVersion = jarVersion;
	}

	public String getJarBuildTime() {
		return jarBuildTime;
	}

	public void setJarBuildTime(String jarBuildTime) {
		this.jarBuildTime = jarBuildTime;
	}

	@Override
	public String toString() {
		return jarName + "   Bundle-Version: " + jarVersion + "   Built-At: " + jarBuildTime;
	}
}

