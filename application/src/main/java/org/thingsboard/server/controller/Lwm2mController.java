/**
 * Copyright © 2016-2023 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.server.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.SaveDeviceWithCredentialsRequest;
import org.thingsboard.server.common.data.device.profile.lwm2m.bootstrap.LwM2MServerSecurityConfigDefault;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.security.DeviceCredentials;
import org.thingsboard.server.service.lwm2m.LwM2MService;

import java.util.Map;

import static org.thingsboard.server.controller.ControllerConstants.IS_BOOTSTRAP_SERVER_PARAM_DESCRIPTION;
import static org.thingsboard.server.controller.ControllerConstants.TENANT_OR_CUSTOMER_AUTHORITY_PARAGRAPH;

@Slf4j
@RestController
@ConditionalOnExpression("('${service.type:null}'=='monolith' || '${service.type:null}'=='tb-core') && '${transport.lwm2m.enabled:false}'=='true'")
@RequestMapping("/api")
public class Lwm2mController extends BaseController {

    @Autowired
    private DeviceController deviceController;

    @Autowired
    private LwM2MService lwM2MService;

    public static final String IS_BOOTSTRAP_SERVER = "isBootstrapServer";

    @ApiOperation(value = "Get Lwm2m Bootstrap SecurityInfo (getLwm2mBootstrapSecurityInfo)",
            notes = "Get the Lwm2m Bootstrap SecurityInfo object (of the current server) based on the provided isBootstrapServer parameter. If isBootstrapServer == true, get the parameters of the current Bootstrap Server. If isBootstrapServer == false, get the parameters of the current Lwm2m Server. Used for client settings when starting the client in Bootstrap mode. " +
                    TENANT_OR_CUSTOMER_AUTHORITY_PARAGRAPH,
            produces = "application/json")
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @GetMapping(value = "/lwm2m/deviceProfile/bootstrap/{isBootstrapServer}")
    @ResponseBody
    public LwM2MServerSecurityConfigDefault getLwm2mBootstrapSecurityInfo(
        @ApiParam(value = IS_BOOTSTRAP_SERVER_PARAM_DESCRIPTION)
        @PathVariable(IS_BOOTSTRAP_SERVER) boolean bootstrapServer) throws ThingsboardException {
            return lwM2MService.getServerSecurityInfo(bootstrapServer);
    }

    @ApiOperation(hidden = true, value = "Save device with credentials (Deprecated)")
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @PostMapping(value = "/lwm2m/device-credentials")
    @ResponseBody
    public Device saveDeviceWithCredentials(@RequestBody Map<Class<?>, Object> deviceWithDeviceCredentials) throws ThingsboardException {
        Device device = checkNotNull(JacksonUtil.convertValue(deviceWithDeviceCredentials.get(Device.class), Device.class));
        DeviceCredentials credentials = checkNotNull(JacksonUtil.convertValue(deviceWithDeviceCredentials.get(DeviceCredentials.class), DeviceCredentials.class));
        return deviceController.saveDeviceWithCredentials(new SaveDeviceWithCredentialsRequest(device, credentials));
    }
}
