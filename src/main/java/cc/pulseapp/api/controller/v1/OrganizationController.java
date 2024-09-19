package cc.pulseapp.api.controller.v1;

import cc.pulseapp.api.model.org.DetailedOrganization;
import cc.pulseapp.api.model.org.Organization;
import cc.pulseapp.api.service.OrganizationService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * This controller is responsible for
 * handling {@link Organization} requests.
 *
 * @author Braydon
 */
@RestController
@RequestMapping(value = "/v1/organization", produces = MediaType.APPLICATION_JSON_VALUE)
public final class OrganizationController {
    /**
     * The organization service to use.
     */
    @NonNull private final OrganizationService orgService;

    @Autowired
    public OrganizationController(@NonNull OrganizationService orgService) {
        this.orgService = orgService;
    }

    /**
     * A GET endpoint to get the
     * organizations of a user.
     *
     * @return the organizations
     */
    @GetMapping("/@me") @ResponseBody @NonNull
    public ResponseEntity<List<DetailedOrganization>> getOrganizations() {
        return ResponseEntity.ok(orgService.getOrganizations());
    }
}