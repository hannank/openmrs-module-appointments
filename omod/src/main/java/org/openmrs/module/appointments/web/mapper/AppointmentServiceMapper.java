package org.openmrs.module.appointments.web.mapper;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Location;
import org.openmrs.api.LocationService;
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.model.ServiceWeeklyAvailability;
import org.openmrs.module.appointments.model.Speciality;
import org.openmrs.module.appointments.service.SpecialityService;
import org.openmrs.module.appointments.web.contract.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Time;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class AppointmentServiceMapper {
    @Autowired
    LocationService locationService;

    @Autowired
    SpecialityService specialityService;

    public AppointmentService getAppointmentServiceFromPayload(AppointmentServicePayload appointmentServicePayload) {
        AppointmentService appointmentService = new AppointmentService();
        if (!StringUtils.isBlank(appointmentServicePayload.getUuid())) {
            appointmentService.setUuid(appointmentServicePayload.getUuid());
        }
        appointmentService.setName(appointmentServicePayload.getName());
        appointmentService.setDescription(appointmentServicePayload.getDescription());
        appointmentService.setDurationMins(appointmentServicePayload.getDurationMins());
        appointmentService.setStartTime(appointmentServicePayload.getStartTime());
        appointmentService.setEndTime(appointmentServicePayload.getEndTime());
        appointmentService.setMaxAppointmentsLimit(appointmentServicePayload.getMaxAppointmentsLimit());

        String locationUuid = appointmentServicePayload.getLocationUuid();
        Location location = locationService.getLocationByUuid(locationUuid);
        appointmentService.setLocation(location);

        String specialityUuid = appointmentServicePayload.getSpecialityUuid();
        Speciality speciality = specialityService.getSpecialityByUuid(specialityUuid);
        appointmentService.setSpeciality(speciality);

        return appointmentService;
    }

    public List<AppointmentServiceDefaultResponse> constructResponse(List<AppointmentService> appointmentServices) {
        return appointmentServices.stream().map(as -> this.mapToDefaultResponse(as, new AppointmentServiceDefaultResponse())).collect(Collectors.toList());
    }

    private AppointmentServiceDefaultResponse mapToDefaultResponse(AppointmentService as, AppointmentServiceDefaultResponse asResponse) {
        asResponse.setUuid(as.getUuid());
        asResponse.setName(as.getName());
        asResponse.setStartTime(convertTimeToString(as.getStartTime()));
        asResponse.setEndTime(convertTimeToString(as.getEndTime()));
        asResponse.setDescription(as.getDescription());
        asResponse.setDurationMins(as.getDurationMins());
        asResponse.setMaxAppointmentsLimit(as.getMaxAppointmentsLimit());

        Map specialityMap = new HashMap();
        Speciality speciality = as.getSpeciality();
        if(speciality != null) {
            specialityMap.put("name", speciality.getName());
            specialityMap.put("uuid", speciality.getUuid());
        }
        asResponse.setSpeciality(specialityMap);

        Map locationMap = new HashMap();
        Location location = as.getLocation();
        if(location != null) {
            locationMap.put("name", location.getName());
            locationMap.put("uuid", location.getUuid());
        }
        asResponse.setLocation(locationMap);

        return asResponse;
    }

    public AppointmentServiceFullResponse constructResponse(AppointmentService service) {
        AppointmentServiceFullResponse response = new AppointmentServiceFullResponse();
        mapToDefaultResponse(service, response);
        Set<ServiceWeeklyAvailability> serviceWeeklyAvailability = service.getWeeklyAvailability();
        if(serviceWeeklyAvailability != null) {
           response.setWeeklyAvailability(serviceWeeklyAvailability.stream().map(availability -> this.constructAvailabilityResponse(availability)).collect(Collectors.toList()));
        }
        return response;
    }

    private Map constructAvailabilityResponse(ServiceWeeklyAvailability availability) {
        Map availabilityMap = new HashMap();
        availabilityMap.put("dayOfWeek",availability.getDayOfWeek());
        availabilityMap.put("startTime", availability.getStartTime());
        availabilityMap.put("endTime", availability.getEndTime());
        availabilityMap.put("maxAppointmentsLimit", availability.getMaxAppointmentsLimit());
        availabilityMap.put("uuid", availability.getUuid());
        return availabilityMap;
    }

    private String convertTimeToString(Time t) {
        if(t == null){
            return new String();
        }
        return t.toString();
    }

}
