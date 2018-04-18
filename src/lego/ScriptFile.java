package lego;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import medcic_proto.MedCic.ENCAPSULATION;


public class ScriptFile
{
	
	static final Logger			logger		= Logger.getLogger("ScriptFile");
	List<ModuleConfiguration>	moduleList	= new ArrayList<ModuleConfiguration>();
	ManagementConfiguration		management	= null;
	String configFilename = "cobfig.lego";

	public ScriptFile()
	{
		setManagement(new ManagementConfiguration());
	}

	public ScriptFile(String Filename) throws FileNotFoundException, IOException
	{
		try (BufferedReader br = new BufferedReader(new FileReader(Filename)))
		{
			String line;
			String TextBlock = "";
			while ((line = br.readLine()) != null)
			{
				if (line.trim().startsWith("#")) continue;

				TextBlock += line;
				if (line.trim().isEmpty())
				{
					if (TextBlock.toLowerCase().startsWith("path"))
					{
						ModuleConfiguration b = new ModuleConfiguration(TextBlock);
						AddModule(b);
					}
					else
					{
						setManagement(new ManagementConfiguration(TextBlock));
					}
					TextBlock = "";
				}
			}
		}
	}
	
	public ScriptFile(String ConfigFilename, String managementHost, int Port) throws FileNotFoundException, IOException
	{
		configFilename = ConfigFilename;
		setManagement(new ManagementConfiguration(managementHost,Port));
	}

	public void AddModule(ModuleConfiguration module)
	{
		moduleList.add(module);
	}

	public void setManagement(ManagementConfiguration mngmnt)
	{
		management = mngmnt;
	}
	
	public Boolean Write()
	{
		return Write(configFilename);
	}

	public Boolean Write(String Filename)
	{
		PrintWriter out = null;
		try
		{
			out = new PrintWriter(Filename);
		}
		catch (FileNotFoundException e)
		{
			return false;
		}

		for (ModuleConfiguration m : moduleList)
		{
			out.print(m.toString());
			out.println();
		}

		out.print(management.toString());

		out.close();

		return true;
	}
	
	public Boolean BuildRecordToFileScript(	 String SourceUri, String IdFile)
	{
		return BuildRecordToFileScript("1",SourceUri, IdFile);
	}

	public Boolean BuildRecordToFileScript(	String Path, String SourceUri, String IdFile)
	{
		AddModule(new ModuleConfiguration(Path, "udpserver", SourceUri, Path + ".1"));
		AddModule(new ModuleConfiguration(Path + ".1", "cesrawinput", null, Path + ".1.1"));
		AddModule(new ModuleConfiguration(Path + ".1.1", "bitoutput",IdFile, ""));

		return true;
	}
	
	public Boolean BuildRecordToFileScript(	String Source1Uri, String IdFile1,
											String Source2Uri, String IdFile2)
	{
		BuildRecordToFileScript("1", Source1Uri, IdFile1);
		BuildRecordToFileScript("2", Source2Uri, IdFile2);
		return true;
	}

	
	public Boolean BuildProductionScript(String Path, ENCAPSULATION Encapsolation, 
			String SourceUri, String DestUri)
			
	{
		AddModule(new ModuleConfiguration(Path, "udpserver", ModuleConfiguration.UriToParam(SourceUri), Path +".1"));
		
		//NewScript.AddModule(new ModuleConfiguration(Path +".1", "cesrawinput", null, Path +".1.1"));
		AddModule(new ModuleConfiguration(Path +".1", "cese1input", "e1", Path +".1.1"));
		

		String module = null;
		String parameters = null;

		switch (Encapsolation)
		{
		case DI:
			logger.warn("DI - encapsulation not supporeted");
			module = null;
			break;

		case DI_PLUS: // "DI++":
			module = "dropinsertpp";
			parameters = "synclength=20,syncword=0xfa85c0,width=2944,mode=cut,0-24,600-610,1186-1196,1772-1782,2358-2368";
			break;

		case EDMAC: // "EDMAC":
			module = "edmac1";
			parameters = "synclength=12,syncword=0xe8c0,width=1008,mode=cut,0-12,204-213,405-414,606-615,807-816";
			break;

		case EDMAC2_2928: // "EDMAC-2 (2928)":
			module = "edmac2";
			parameters = "synclength=12,syncword=0xe8c0,width=2928,mode=cut,0-12,204-213,405-414,606-615,807-816";
			break;

		case EDMAC2_3072: // "EDMAC-2 (3072)":
			module = "edmac3072";
			parameters = "synclength=12,syncword=0xe8c0,width=3072,mode=cut,0-12,204-213,405-414,606-615,807-816";
			break;

		case ESC_532: // "ESC++ (532)":
			module = "escplusplus532";
			parameters = "synclength=12,syncword=0xe8c0,width=532,mode=cut,0-12,20-27,36-45";
			break;

		case ESC_874:// "ESC++ (874)":
			module = "escplusplus874";
			parameters = "synclength=12,syncword=0xe8c0,width=874,mode=cut,0-12,20-28,36-45,146-155,255-264,364-373,473-482,582-591,691-700,800-809";
			break;

		case ESC_1104: // "ESC++ (1104)":
			module = "escplusplus1104";
			parameters = "synclength=12,syncword=0xe8c0,width=1104,mode=cut,0-12,20-28,36-45,174-183,312-321,450-459,588-597,726-735,864-873,1002-1011";
			break;

		case ESC_1792: // "ESC++ (1792)":
			module = "escplusplus1792";
			parameters = "synclength=12,syncword=0xe8c0,width=1792,mode=cut,0-12,20-27,36-45";
			break;

		case ESC_551: // "ESC++ (551)":
			logger.warn("ESC_551 - encapsulation not supporeted");
			module = null;
			break;

		case E2:// "E2":
			logger.warn("E2 - encapsulation not supporeted");
			module = null;
			break;

		case UNRECOGNIZED:
		default:
			logger.warn("UNRECOGNIZED - encapsulation not supporeted");
			module = null;
			break;
		}

		if (module == null) return null;

		AddModule(new ModuleConfiguration(Path +".1.1", module, parameters, Path +".1.1.1"));
		AddModule(new ModuleConfiguration(Path +".1.1.1", "cesrawout", null, Path +".1.1.1.1"));
		AddModule(new ModuleConfiguration(Path +".1.1.1.1", "udpclient", ModuleConfiguration.UriToParam(DestUri), Path +".1.1.1.1,1"));
		AddModule(new ModuleConfiguration(Path +".1.1.1.1,1", "packet2null", "", ""));

		return true;
	}
	
	
	public Boolean BuildProductionScript(ENCAPSULATION Encapsolation, 
			String Source1Uri, String Dest1Uri,
			String Source2Uri, String Dest2Uri)
	{
		BuildProductionScript("1", Encapsolation,  Source1Uri, Dest1Uri);
		BuildProductionScript("2", Encapsolation,  Source2Uri, Dest2Uri);
		return true;
	}

}
