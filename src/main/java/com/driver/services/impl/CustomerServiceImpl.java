package com.driver.services.impl;

import com.driver.model.*;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function

		Customer customer=customerRepository2.findById(customerId).get();
		customerRepository2.delete(customer);
	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query

		List<Driver> driverList=driverRepository2.findAll();
		int id=Integer.MAX_VALUE;
		for(Driver d:driverList){
			if(d.getDriverId()<id){
				Cab cab=d.getCab();
				if(cab.getAvailable()){
					id=d.getDriverId();
				}
			}
		}
		if(id==Integer.MAX_VALUE){

			throw new Exception("No cab available!");
		}
		TripBooking tripBooking=new TripBooking();
		tripBooking.setFromLocation(fromLocation);
		tripBooking.setToLocation(toLocation);
		tripBooking.setDistanceInKm(distanceInKm);
		tripBooking.setStatus(TripStatus.CONFIRMED);
		Customer customer=customerRepository2.findById(customerId).get();

		Driver driver=driverRepository2.findById(id).get();
		driver.getCab().setAvailable(false);
		tripBooking.setDriver(driver);
		tripBooking.setBill(distanceInKm*driver.getCab().getPerKmRate());
		tripBooking.setCustomer(customer);


//		tripBookingRepository2.save(tripBooking);
		List<TripBooking> tripBookingList=customer.getTripBookingList();
		tripBookingList.add(tripBooking);
		customer.setTripBookingList(tripBookingList);
		customerRepository2.save(customer);

		List<TripBooking> tripBookingList1=driver.getTripBookingList();
		tripBookingList1.add(tripBooking);
		driver.setTripBookingList(tripBookingList1);

		driverRepository2.save(driver);
		return tripBooking;

	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly

		TripBooking tripBooking=tripBookingRepository2.findById(tripId).get();
		tripBooking.setStatus(TripStatus.CANCELED);
		tripBooking.setBill(0);

		tripBooking.getDriver().getCab().setAvailable(true);

//		driverRepository2.save(tripBooking.getDriver());
		tripBookingRepository2.save(tripBooking);

	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly

		TripBooking tripBooking=tripBookingRepository2.findById(tripId).get();
		tripBooking.setStatus(TripStatus.COMPLETED);
		tripBooking.getDriver().getCab().setAvailable(true);
//		driverRepository2.save(tripBooking.getDriver());
		tripBookingRepository2.save(tripBooking);

	}
}
