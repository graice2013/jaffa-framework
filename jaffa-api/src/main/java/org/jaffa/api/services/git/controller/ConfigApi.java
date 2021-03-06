/*
 * ====================================================================
 * JAFFA - Java Application Framework For All
 *
 * Copyright (C) 2002 JAFFA Development Group
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Redistribution and use of this software and associated documentation ("Software"),
 * with or without modification, are permitted provided that the following conditions are met:
 * 1.	Redistributions of source code must retain copyright statements and notices.
 *         Redistributions must also contain a copy of this document.
 * 2.	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 * 3.	The name "JAFFA" must not be used to endorse or promote products derived from
 * 	this Software without prior written permission. For written permission,
 * 	please contact mail to: jaffagroup@yahoo.com.
 * 4.	Products derived from this Software may not be called "JAFFA" nor may "JAFFA"
 * 	appear in their names without prior written permission.
 * 5.	Due credit should be given to the JAFFA Project (http://jaffa.sourceforge.net).
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 */
package org.jaffa.api.services.git.controller;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jaffa.api.ConfigApiCore;
import org.jaffa.api.cluster.IClusterMetadataDAO;
import org.jaffa.api.services.git.LinkResolver;
import org.jaffa.loader.ResourceLoader;
import org.jaffa.rules.AopXmlLoader;
import org.apache.log4j.*;
import org.jaffa.api.FileContents;
import org.jaffa.api.cluster.NodeInformation;
import org.jaffa.security.filter.FileFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

/**
 * ConfigAPI - A controller for REST service endpoints that provides the ability to dynamically manipulate
 * GOLD configurations and properties through the use of customizable ZIP files.
 *
 * @author Matthew Wayles
 * @version 1.0
 */
@RestController
@RequestMapping("/git")
public class ConfigApi implements IConfigApi {
    private static final String FILE_EXTENSION = "zip";
    private static final int BYTE_ARRAY_INIT_LENGTH = 17;
    private static final Logger log = Logger.getLogger(ConfigApi.class);

    private static File gctConfigDirectory = ResourceLoader.customConfigPath!=null ? new File(ResourceLoader.customConfigPath) : null;
    private Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    @Autowired
//    @Qualifier("ClusterMetadataDAO")
    IClusterMetadataDAO clusterMetadataDAO;

    /**
     * If the user accesses the root of the GIT services, redirect them to the CXF Services page to avoid
     * sending a 404 error
     *
     * @return A redirect HTTP response to CXF Services
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public Response rootRedirectToServices() {
        log.info("Request for jaffa API GIT root received; redirecting to CXF Services");
        return Response
                .status(Response.Status.FOUND)
                .header("Location", "/api/services")
                .build();
    }


    /**
     * Corresponds to DELETE endpoint. Unregister all configurations within the provided compressed file and delete
     * the file itself
     *
     * @param compressedFile The custom compressed file located in GCT_CONFIG to be deleted
     * @return HTTP Response indicating success or failure
     * @throws IOException When the endpoint has difficulty accessing or removing the file
     */
    @RequestMapping(value = "/config", method = RequestMethod.DELETE)
    public Response deleteCustomConfigFile(String compressedFile) throws IOException {
        String fileNameToDelete = this.formatUserInput(compressedFile);

        File fileToDelete = (gctConfigDirectory!=null && fileNameToDelete != null) ?
                new File(gctConfigDirectory + File.separator + fileNameToDelete) : null;

        //Provide HTTP error if compressed file does not exist on server
        if (fileToDelete==null || !fileToDelete.exists()) {
            log.warn("The requested compressed file " + fileToDelete + " was not found in " + gctConfigDirectory);
            return Response
                    .status(Response.Status.NO_CONTENT)
                    .entity("The requested compressed file could not be found on the server.")
                    .build();
        }

        //Unregister configurations, delete file, and update metadata
        File tempDir = ConfigApiCore.extractToTemporaryDirectory(fileToDelete);
        if (tempDir != null) {
            removeConfigurations(fileToDelete, tempDir);
            removeZipFile(fileToDelete);
            removeMetadata(fileNameToDelete);
        } else {
            log.error("Failed to extract " + fileToDelete + " to temporary directory.");
        }

        Response.ResponseBuilder response = Response.status(Response.Status.OK);
        return response.build();
    }


    /**
     * Corresponds to GET/compressedFile Endpoint - Download the requested compressed file
     * (when authenticated through basic auth) or return a BAD REQUEST error if the file does not exist
     *
     * @param compressedFile The compressed file to download
     * @return The compressed file as an attachment to download
     */
    @RequestMapping(value = "/config/{compressedFile}", method = RequestMethod.GET)
    public Response getCustomConfigFile(@PathVariable String compressedFile) {
        String fileNameToDownload = this.formatUserInput(compressedFile);
        File fileToDownload = gctConfigDirectory!=null && fileNameToDownload != null ?
                new File(gctConfigDirectory + File.separator + fileNameToDownload) : null;

        return (fileToDownload == null || !fileToDownload.exists() ?
                Response.status(Response.Status.BAD_REQUEST)
                        .entity("The server processed your request, but cannot find a file located at " + fileToDownload)
                        .build()
                : Response.status(Response.Status.OK)
                .entity(fileToDownload)
                .header("Content-Disposition", "attachment; filename=" + fileNameToDownload)
                .build());
    }


    /**
     * Corresponds to GET endpoint - Retrieve a list of all the compressed files contained in the GCT_CONFIG property
     * location that match FILE_EXTENSION. In addition to the file names, the method also returns the Context Salience
     * and file contents.
     *
     * @return A JSON representation of the compressed files and their contents
     * @throws IOException Thrown when the directory cannot be accessed or compressed files cannot be read
     */
    @RequestMapping(value = "/config", method = RequestMethod.GET)
    public Response getCustomConfigFileList() throws IOException {
        File[] allFilesInDirectory = gctConfigDirectory!=null ? gctConfigDirectory.listFiles() : null;
        List<File> compressedFilesInDirectory = getCompressedFiles(allFilesInDirectory);
        List<FileContents> compressedFilesContents = convertToFileContents(compressedFilesInDirectory);

        Response.ResponseBuilder response = Response.ok(gson.toJson(compressedFilesContents));
        response.header("Content-Type", MediaType.APPLICATION_JSON);
        return response.build();
    }


    /**
     * Corresponds to the PUT endpoint - Upload a configuration compressed file to the server
     *
     * @param compressedFile The name of the uploaded compressed file
     * @param payload        The binary data from the client's compressed file
     * @return HTTP Response indicating success or failure
     * @throws IOException When the endpoint has difficulty accessing or uploading the file
     */
    @RequestMapping(value = "/config/{compressedFile}", method = RequestMethod.POST)
    public Response postCustomConfigFile(@PathVariable String compressedFile, byte[] payload) throws IOException {
        String fileNameToPost = this.formatUserInput(compressedFile);
        File fileToPost = gctConfigDirectory!=null && fileNameToPost != null ?
                new File(gctConfigDirectory + File.separator + fileNameToPost) : null;

        if (fileToPost == null || payload.length <= BYTE_ARRAY_INIT_LENGTH || fileToPost.exists()) {
            return postError(payload, fileToPost);
        }

        //create directory if not exists
        if (!fileToPost.getParentFile().exists()) {
            fileToPost.getParentFile().mkdirs();
        }

        //Copy files to server and register resources
        Files.copy(new ByteArrayInputStream(payload), fileToPost.toPath());

        Response.ResponseBuilder response;
        File tempDir = ConfigApiCore.extractToTemporaryDirectory(fileToPost);
        if (tempDir != null) {
            registerConfigurations(fileToPost, tempDir);
            log.info("The resources in " + fileToPost + " have finished registering configurations to the repositories");
            response = Response.status(Response.Status.OK);
        } else {
            removeZipFile(fileToPost);
            response = Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("Upload Failed. The uploaded ZIP file must contain ONLY a META-INF directory containing " +
                            "configuration files and a manifest file.");
        }
        registerMetadata(fileToPost);
        return response.build();
    }

    /**
     * Corresponds to the GET endpoint for clusterMetadataDAO. Retrieves cluster node metadata.
     *
     * @return JSON Response with metadata from each node
     */
//    @Override
    @RequestMapping(value = "/config/clusterMetadata", method = RequestMethod.GET)
    public Response getClusterMetadata() {
        Map<String, NodeInformation> allNodesMetadata = clusterMetadataDAO.getClusterMetadata();

        for (Map.Entry<String, NodeInformation> node : allNodesMetadata.entrySet()) {
            NodeInformation currentNodeInformation = node.getValue();
            currentNodeInformation.getConfig().forEach(LinkResolver::addLinks);
            LinkResolver.addLinks(currentNodeInformation);
        }
        log.info("Successfully retrieved metadata from storage.");

        Response.ResponseBuilder response = Response.ok(gson.toJson(allNodesMetadata));
        response.header("Content-Type", MediaType.APPLICATION_JSON);
        return response.build();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////  HELPER METHODS  ///////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Inject this class into application context to retrieve application beans
     */
    @PostConstruct
    public void init() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

    /**
     * Verify that the user supplied a file extension in the URL, and add it if not
     *
     * @param fileName  The user URL input
     * @param extension The file extension expected (defaults to ZIP)
     * @return The user input with appended file extension
     */
    private String verifyExtension(String fileName, String extension) {
        if (!fileName.endsWith(extension)) {
            fileName += "." + extension;
        }
        return fileName;
    }

    /**
     * Scrub user input for malicious int protection, and append '.zip' extensio nif it was omitted
     * @param fileName  The user input filename
     * @return  The scrubbed, extended user input filename
     */
    private String formatUserInput(String fileName) {
        fileName = this.verifyExtension(fileName, FILE_EXTENSION);
        return FileFilter.filterUserInputFileName(fileName);
    }

    /**
     * Retrieve a list of compressed files in the GCT_CONFIG location
     * @param directoryFileList The list of all files in the directory
     * @return The list of compressed files in the directory
     */
    private List<File> getCompressedFiles(File[] directoryFileList) {
        List<File> compressedFiles = new ArrayList<>();

        if (directoryFileList != null) {
            for (File file : directoryFileList) {
                if (file.getName().endsWith(FILE_EXTENSION)) {
                    compressedFiles.add(file);
                }
            }
        }
        else {
            log.warn("GCT_CONFIG is not set, or is empty." +
                    " Please check your GCT_CONFIG variable. It" +
                    " is currently returning: " + gctConfigDirectory);
        }
        return compressedFiles;
    }

    /**
     * Convert a list of custom configuration files to FileContents objects for json serialization
     *
     * @param compressedFileList The list of custom configuration files to be converted
     * @return A list of converted FileContents objects
     * @throws IOException If any of the files cannot be accessed or converted
     */
    private List<FileContents> convertToFileContents(List<File> compressedFileList) throws IOException {
        List<FileContents> fileContents = new ArrayList<>();
        if(compressedFileList!=null) {
            for (File compressedFile : compressedFileList) {
                FileContents fileInformation = ConfigApiCore.getFileContents(compressedFile);
                LinkResolver.addLinks(fileInformation);
                fileContents.add(fileInformation);
            }
        }
        return fileContents;
    }

    /**
     * Register configurations in a custom config file to IManager repositories
     *
     * @param filePath The path of the configuration file
     * @param tempDir  The temporary directory storing the configurations
     * @throws IOException If any directories or files cannot be accessed or parsed
     */
    private void registerConfigurations(File filePath, File tempDir) throws IOException {
        FileContents fileContents = ConfigApiCore.getFileContents(filePath);
        ConfigApiCore.registerResources(tempDir, fileContents);
        String tempDirPath = tempDir.getPath();
        AopXmlLoader.getInstance().processAopPath(tempDirPath);
        ConfigApiCore.removeDirTree(tempDir);
    }

    /**
     * Add a configuration file to the metadata inside of a NodeInformation object
     * @param filePath The configuration file to add to the node metadata
     * @throws IOException If the configuration file cannot be accessed
     */
    private void registerMetadata(File filePath) throws IOException {
        FileContents fileInformation = ConfigApiCore.getFileContents(filePath);
        Map<String, NodeInformation> allNodesMetadata = clusterMetadataDAO.getClusterMetadata();
        String nodeId = NodeInformation.getLoadBalancerNodeId();
        NodeInformation node = allNodesMetadata.get(nodeId);
        node.getConfig().add(fileInformation);
        clusterMetadataDAO.put(nodeId, node);
        log.info("Successfully added file " + filePath + " to node metadata for " + nodeId);
    }

    /**
     * Unregister configurations from a custom config file in IManager repositories
     * @param filePath The path of the configuration file
     * @param tempDir  The temporary directory storing the configurations
     * @throws IOException If any directories or files cannot be accessed or parsed
     */
    private void removeConfigurations(File filePath, File tempDir) throws IOException {
        ConfigApiCore.unregisterResources(tempDir, ConfigApiCore.getFileContents(filePath));
        AopXmlLoader.getInstance().unloadAop(tempDir.getPath());
        ConfigApiCore.removeDirTree(tempDir);
    }

    /**
     * Remove a configuration file from a NodeInformation object's metadata
     * @param fileToDelete The configuration file to delete from the metadata
     */
    private void removeMetadata(String fileToDelete) {
        Map<String, NodeInformation> allNodesMetadata = clusterMetadataDAO.getClusterMetadata();
        String nodeId = NodeInformation.getLoadBalancerNodeId();
        NodeInformation node = allNodesMetadata.get(nodeId);
        for (FileContents configFile : node.getConfig()) {
            if (configFile.getName().equals(fileToDelete)) {
                node.getConfig().remove(configFile);
                clusterMetadataDAO.put(nodeId, node);
                log.info("Successfully removed file " + fileToDelete +
                        " from node metadata for " + nodeId);
                break;
            }
        }
    }

    /**
     * In certain cases, a configuration archive cannot be
     * immediately removed from the filesystem because
     * its contents are still undergoing removal operations
     * of their own. This method allows the contents to
     * complete their processes and release the file handle
     * so that the zip parent can be removed
     * @param file The configuration archive to remove
     */
    private void removeZipFile(File file) {
        try {
            Files.deleteIfExists(file.toPath());
            log.info("Successfully removed " + file.getName() + " from the filesystem");
        } catch (IOException ex) {
            log.error("Could not remove the file "
                    + file.getName() + " from the filesystem", ex);
        }
    }

    /**
     * Provide common HTTP error responses encountered during POST request
     *
     * @param payload  The binary data sent in the POST request body
     * @param filePath The destination filepath of the uploaded file
     * @return An HTTP response containing an error code and verbose error message
     */
    private Response postError(byte[] payload, File filePath) {
        Response response = null;
        if (filePath == null) {
            log.warn("GCT_DIRECTORY property is not set");
            response = Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("Unable to complete this request because the GCT_DIRECTORY property is not set.")
                    .build();
        }
        else if (payload.length <= BYTE_ARRAY_INIT_LENGTH) {
            log.warn("A file was not included in your API POST request");
            response = Response
                    .status(Response.Status.NO_CONTENT)
                    .entity("A compressed file was not included in the request.")
                    .build();
        } else if (filePath.exists()) {
            log.warn("The API POST request failed because " + filePath.getName() + " exists in this location already");
            response = Response
                    .status(Response.Status.CONFLICT)
                    .entity("The file " + filePath.getName() + " exists in this location already.")
                    .build();
        }
        return response;
    }
}



