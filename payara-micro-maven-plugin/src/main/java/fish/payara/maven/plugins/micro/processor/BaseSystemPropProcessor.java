/*
 *
 * Copyright (c) 2017-2019 Payara Foundation and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://github.com/payara/Payara/blob/master/LICENSE.txt
 * See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * The Payara Foundation designates this particular file as subject to the "Classpath"
 * exception as provided by the Payara Foundation in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package fish.payara.maven.plugins.micro.processor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.twdata.maven.mojoexecutor.MojoExecutor;

import static org.apache.commons.lang.StringEscapeUtils.escapeJava;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

/**
 * @author ondrejmihalyi
 */
public abstract class BaseSystemPropProcessor extends BaseProcessor {

    private static final boolean APPEND = true;
    private static final String PAYARA_BOOT_PROP_FILE = "payara-boot.properties";
    private MavenProject mavenProject;

    protected void addSystemPropertiesForPayaraMicro(Properties properties, String comment, MojoExecutor.ExecutionEnvironment environment) throws MojoExecutionException {
        Properties existingProperties = new Properties();

        try(InputStream inputStream = new FileInputStream(mavenProject.getBuild().getDirectory() + EXTRACTED_PAYARAMICRO_FOLDER + MICROINF_FOLDER + File.separator + PAYARA_BOOT_PROP_FILE)) {
            existingProperties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        executeMojo(plainTextPlugin,
                goal("write"),
                configuration(
                        element(name("outputDirectory"), OUTPUT_FOLDER + MICROINF_FOLDER),
                        element(name("files"),
                                element(name("file"),
                                        element(name("name"), PAYARA_BOOT_PROP_FILE),
                                        element(name("append"), String.valueOf(APPEND)),
                                        element(name("lines"),
                                                constructElementsForProperties(properties, existingProperties, comment)
                                        )
                                )
                        )
                ),
                environment
        );
    }

    private Element[] constructElementsForProperties(Properties properties, Properties existingProperties, String comment) {
        List<Element> elements = new ArrayList<>();
        String commentLine = "\n# " + ((comment != null) ? comment : "Additional properties");
        Element emptyLine = element(name("line"), commentLine);
        elements.add(emptyLine);
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            Element element = element(
                    name("line"),
                    (existingProperties.containsKey(entry.getKey()) ? "#" : "") +
                    escapeJava(entry.getKey() + "=" + entry.getValue())
            );
            elements.add(element);
        }
        return elements.toArray(new Element[elements.size()]);
    }

    public BaseSystemPropProcessor set(MavenProject mavenProject) {
        this.mavenProject = mavenProject;
        return this;
    }
}
