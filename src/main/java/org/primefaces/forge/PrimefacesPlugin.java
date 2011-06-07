package org.primefaces.forge;

import java.io.InputStream;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.JavaSourceFacet;
import org.jboss.forge.project.facets.WebResourceFacet;
import org.jboss.forge.project.facets.events.InstallFacets;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.shell.ShellColor;
import org.jboss.forge.shell.ShellMessages;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.Command;
import org.jboss.forge.shell.plugins.DefaultCommand;
import org.jboss.forge.shell.plugins.Option;
import org.jboss.forge.shell.plugins.PipeOut;
import org.jboss.forge.shell.plugins.Plugin;
import org.jboss.forge.shell.plugins.RequiresProject;

@Alias("primefaces")
@RequiresProject
public class PrimefacesPlugin implements Plugin
{

   private final Project project;
   private final Event<InstallFacets> installFacets;

   @Inject
   public PrimefacesPlugin(final Project project, final Event<InstallFacets> event)
   {
      this.project = project;
      this.installFacets = event;
   }

   @DefaultCommand
   public void status(final PipeOut out)
   {
      if (project.hasFacet(PrimefacesFacet.class))
      {
         out.println("Primeaces is installed.");
      }
      else
      {
         out.println("Primeaces is not installed. Use 'primefaces setup' to get started.");
      }
   }

   // confirmed working
   @Command("setup")
   public void setup(final PipeOut out)
   {
      if (!project.hasFacet(PrimefacesFacet.class))
      {
         installFacets.fire(new InstallFacets(PrimefacesFacet.class));
      }
      if (project.hasFacet(PrimefacesFacet.class))
      {
         ShellMessages.success(out, "PrimefacesFacet is configured.");
      }
   }

   private void assertInstalled()
   {
      if (!project.hasFacet(PrimefacesFacet.class))
      {
         throw new RuntimeException("PrimefacesFacet is not installed. Use 'primefaces setup' to get started.");
      }
   }

   @Command("help")
   public void exampleDefaultCommand(@Option final String opt, final PipeOut pipeOut)
   {
      pipeOut.println(ShellColor.BLUE, "Use the install commands to install:");
      pipeOut.println(ShellColor.BLUE, "  install-example-facelet: a sample Primeaces enabled facelet file");
   }

   @Command("install-example-facelet")
   public void installExampleFacelets(final PipeOut pipeOut)
   {
      assertInstalled();
      createFaceletFiles(pipeOut);  
      createPrimeBean(pipeOut);
   }

   /**
    * Create a simple template file, and a Primeaces enabled index file that uses the template
    * 
    * @param pipeOut
    */
   private void createFaceletFiles(final PipeOut pipeOut)
   {
      DirectoryResource webRoot = project.getFacet(WebResourceFacet.class).getWebRootDirectory();
      DirectoryResource templateDirectory = webRoot.getOrCreateChildDirectory("templates");
      FileResource<?> templatePage = (FileResource<?>) templateDirectory.getChild("template.xhtml");
      InputStream stream = PrimefacesPlugin.class.getResourceAsStream("/org/primefaces/forge/template.xhtml");
      templatePage.setContents(stream);
      pipeOut.println(ShellColor.YELLOW, String.format(PrimefacesFacet.SUCCESS_MSG_FMT, "template.xhtml", "file"));

      FileResource<?> indexPage = (FileResource<?>) webRoot.getChild("index.xhtml");
      stream = PrimefacesPlugin.class.getResourceAsStream("/org/primefaces/forge/index.xhtml");
      indexPage.setContents(stream);
      pipeOut.println(ShellColor.YELLOW, String.format(PrimefacesFacet.SUCCESS_MSG_FMT, "index.xhtml", "file"));

      FileResource<?> forgeIndexPage = (FileResource<?>) webRoot.getChild("index.html");
      String contents;
      // TODO: if (contents.contains("Welcome to Seam Forge")) {
      forgeIndexPage.delete();
   }

   /**
    * Create a simple JSF managed bean to back the Primeaces input in the example facelet file
    * 
    * @param pipeOut
    */
   private void createPrimeBean(final PipeOut pipeOut)
   {
      JavaSourceFacet source = project.getFacet(JavaSourceFacet.class);
      DirectoryResource sourceRoot = source.getBasePackageResource();
      FileResource<?> indexPage = (FileResource<?>) sourceRoot.getChild("PrimeBean.java");
      InputStream stream = PrimefacesPlugin.class.getResourceAsStream("/org/primefaces/forge/PrimeBean.java.txt");
      indexPage.setContents(stream);
      pipeOut.println(ShellColor.YELLOW, String.format(PrimefacesFacet.SUCCESS_MSG_FMT, "PrimeBean", "class"));
   }
}