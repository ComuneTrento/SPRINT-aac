/*******************************************************************************
 * Copyright 2015 Fondazione Bruno Kessler
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 ******************************************************************************/
package it.smartcommunitylab.aac;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mangofactory.swagger.configuration.SpringSwaggerConfig;
import com.mangofactory.swagger.models.dto.ApiInfo;
import com.mangofactory.swagger.plugin.EnableSwagger;
import com.mangofactory.swagger.plugin.SwaggerSpringMvcPlugin;

/**
 * @author raman
 *
 */
@Configuration
@EnableSwagger
public class SwaggerConfig {

	private SpringSwaggerConfig springSwaggerConfig;

	   @Autowired
	   public void setSpringSwaggerConfig(SpringSwaggerConfig springSwaggerConfig) {
	      this.springSwaggerConfig = springSwaggerConfig;
	   }

	   /**
	    * Every SwaggerSpringMvcPlugin bean is picked up by the swagger-mvc framework - allowing for multiple
	    * swagger groups i.e. same code base multiple swagger resource listings.
	    */
	   @Bean
	   public SwaggerSpringMvcPlugin customImplementation() {
	      return new SwaggerSpringMvcPlugin(this.springSwaggerConfig)
	    		  .apiInfo(apiInfo())
	              .includePatterns(".*profile.*", ".*resources/access.*"/*,".*oauth/token.*"*/)
	              ;
	   }
	   
	   private ApiInfo apiInfo() {
		      ApiInfo apiInfo = new ApiInfo(
		              "AAC API",
		              "SIMPATICO AAC APIs for profile information and access validation",
		              "",
		              "info@simpatico-project.eu",
		              "Apache License, Version 2.0",
		              "http://www.apache.org/licenses/LICENSE-2.0"
		        );
		      return apiInfo;
		    }
}
