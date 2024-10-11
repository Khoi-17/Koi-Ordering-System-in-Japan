package fall24.swp391.KoiOrderingSystem.service;

import fall24.swp391.KoiOrderingSystem.enums.BookingType;
import fall24.swp391.KoiOrderingSystem.enums.PaymentStatus;
import fall24.swp391.KoiOrderingSystem.enums.Role;
import fall24.swp391.KoiOrderingSystem.exception.*;
import fall24.swp391.KoiOrderingSystem.model.request.BookingKoiRequest;
import fall24.swp391.KoiOrderingSystem.model.request.BookingRequest;
import fall24.swp391.KoiOrderingSystem.model.response.BookingResponse;
import fall24.swp391.KoiOrderingSystem.pojo.*;
import fall24.swp391.KoiOrderingSystem.repo.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookingService implements IBookingService{

    @Autowired
    private IBookingRepository bookingRepository;

    @Autowired
    private IBookingTourDetailRepository iBookingTourDetailRepository;

    @Autowired
    private ITourRepository iTourRepository;

    @Autowired
    private IKoisRepository iKoisRepository;

    @Autowired
    private IBookingKoiDetailRepository iBookingKoiDetailRepository;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public BookingResponse createTourBooking(BookingRequest bookingRequest) throws Exception{
        try {
            Account account = authenticationService.getCurrentAccount();
            if(account.getRole() == Role.CUSTOMER){
                Tours tours = iTourRepository.findById(bookingRequest.getTourID())
                        .orElseThrow(() -> new NotFoundEntity("Tour not found"));
                //nếu lỗi là say bye
                Bookings booking = new Bookings();
                booking.setPaymentMethod(bookingRequest.getPaymentMethod());
                booking.setCreatedBy(account);
                booking.setPaymentStatus(PaymentStatus.pending);// Set default status to pending
                booking.setBookingType(BookingType.BookingForTour);
                bookingRepository.save(booking);
                //Save booking
                BookingTourDetail bookingTourDetail = new BookingTourDetail(booking, tours, bookingRequest.getParticipants());
                bookingTourDetail.setTotalAmount(tours.getUnitPrice() * bookingTourDetail.getParticipant());
                iBookingTourDetailRepository.save(bookingTourDetail);
                float totalBookingAmount = 0;
                List<BookingTourDetail> tourDetailOfBookingID = iBookingTourDetailRepository.showDetailOfBookingID(booking.getId());
                for(BookingTourDetail b : tourDetailOfBookingID){
                    totalBookingAmount += b.getTotalAmount();
                }
                booking.setTotalAmount(totalBookingAmount);
                booking.setTotalAmountWithVAT(booking.getTotalAmount() + booking.getVatAmount() - booking.getDiscountAmount());
                bookingRepository.save(booking);
                BookingResponse bookingResponse = modelMapper.map(booking, BookingResponse.class);
                bookingResponse.setCreatedBy(account.getFirstName()+ " " + account.getLastName());
                return bookingResponse;
            } else {
                throw new NotCreateException("Create Booking Only Role Manager");
            }
        } catch (Exception e) {
            throw new GenericException(e.getMessage());
        }
    }

    @Override
    public List<Bookings> getTourBooking(Long accountID) {
        return bookingRepository.listTourBooking(accountID);
    }

    @Override
    public Bookings updateTourBooking(Long id, Bookings bookingUpdateDetail) {
        try {
            Optional<Bookings> existingBooking = bookingRepository.findById(id);
            if (existingBooking.isPresent()) {
                Bookings bookingToUpdate = existingBooking.get();
                // Update fields except paymentStatus if it's pending or cancelled
                if (bookingToUpdate.getPaymentStatus() != PaymentStatus.pending &&
                        bookingToUpdate.getPaymentStatus() != PaymentStatus.cancelled) {
                    bookingToUpdate.setPaymentStatus(bookingUpdateDetail.getPaymentStatus());
                }
                bookingToUpdate.setPaymentMethod(bookingToUpdate.getPaymentMethod());
                bookingToUpdate.setDiscountAmount(bookingUpdateDetail.getDiscountAmount());
                bookingToUpdate.setVat(bookingUpdateDetail.getVat());
                bookingToUpdate.setVatAmount(bookingToUpdate.getVat() * bookingToUpdate.getTotalAmount());
                bookingToUpdate.setUpdatedBy(authenticationService.getCurrentAccount());
                float totalBookingAmount = 0;
                List<BookingTourDetail> tourDetailOfBookingID = iBookingTourDetailRepository.showDetailOfBookingID(id);
                for(BookingTourDetail b : tourDetailOfBookingID){
                    totalBookingAmount += b.getTotalAmount();
                }
                bookingToUpdate.setTotalAmount(totalBookingAmount);
                bookingToUpdate.setTotalAmountWithVAT(bookingToUpdate.getTotalAmount() + bookingToUpdate.getVatAmount() - bookingToUpdate.getDiscountAmount());
                return bookingRepository.save(bookingToUpdate);
            } else {
                throw new NotUpdateException("Update booking id " + id + " failed");
            }
        } catch (NotUpdateException e) {
            throw new GenericException(e.getMessage());
        }
    }

    //delete means update payment_status to cancelled
    @Override
    public Bookings deleteBooking(Long id) {
        try {
            Optional<Bookings> existingBooking = bookingRepository.findById(id);
            if (existingBooking.isPresent()) {
                Bookings bookingToUpdate = existingBooking.get();
                bookingToUpdate.setPaymentStatus(PaymentStatus.cancelled); // Update status to cancelled
                return bookingRepository.save(bookingToUpdate); // Save the updated booking
            } else {
                throw new NotDeleteException("Can not DELETE booking " + id);
            }
        } catch (Exception e) {
            throw new GenericException(e.getMessage());
        }
    }

    @Override
    public BookingResponse createKoiBooking(BookingKoiRequest bookingKoiRequest) {
        try{
            Account account = authenticationService.getCurrentAccount();
            if(account.getRole()!=Role.SALES_STAFF){
                Kois kois = iKoisRepository.findById(bookingKoiRequest.getKoiId())
                        .orElseThrow(() -> new NotFoundEntity("Koi Tour not found"));


                Bookings booking = new Bookings();
                booking.setPaymentMethod(bookingKoiRequest.getPaymentMethod());
                booking.setCreatedBy(account);
                booking.setPaymentStatus(PaymentStatus.pending);
                booking.setBookingType(BookingType.BookingForKoi);
                bookingRepository.save(booking);

                for(BookingKoiDetail koidetail: bookingKoiRequest.getKoiDetails()) {
                    BookingKoiDetail bookingKoiDetail = new BookingKoiDetail(booking, kois, koidetail.getQuantity());
                    bookingKoiDetail.setTotalAmount(kois.getUnitPrice() * koidetail.getQuantity());
                    iBookingKoiDetailRepository.save(bookingKoiDetail);
                }
                    float totalBookingAmount = 0;
                    List<BookingKoiDetail> koiDetailOfBookingID = iBookingKoiDetailRepository.showDetailOfBookingID(booking.getId());
                    for (BookingKoiDetail b : koiDetailOfBookingID) {
                        totalBookingAmount += b.getTotalAmount();
                    }
                    booking.setTotalAmount(totalBookingAmount);
                    booking.setTotalAmountWithVAT(booking.getTotalAmount() + booking.getVatAmount() - booking.getDiscountAmount());
                    bookingRepository.save(booking);

                    BookingResponse bookingResponse = modelMapper.map(booking, BookingResponse.class);
                    bookingResponse.setCreatedBy(account.getFirstName() + " " + account.getLastName());
                    return bookingResponse;
            }else{
                throw new NotCreateException("Create Booking Only Role STAFF");
            }
        }catch (Exception e) {
            throw new GenericException(e.getMessage());
        }
    }

    @Override
    public Bookings updateKoiBooking(Long id, Bookings bookingDetails) {
       try{
            Optional<Bookings> existingBooking =bookingRepository.findById(id);
            if(existingBooking.isPresent()){
                Bookings bookingKoiToUpdate =existingBooking.get();
                if(bookingKoiToUpdate.getPaymentStatus() !=PaymentStatus.cancelled &&
                        bookingKoiToUpdate.getPaymentStatus()!=PaymentStatus.pending){
                    bookingKoiToUpdate.setPaymentStatus((bookingDetails.getPaymentStatus()));
                }

                bookingKoiToUpdate.setPaymentMethod(bookingDetails.getPaymentMethod());
                bookingKoiToUpdate.setDiscountAmount(bookingDetails.getDiscountAmount());
                bookingKoiToUpdate.setVat(bookingDetails.getVat());
                bookingKoiToUpdate.setVatAmount(bookingKoiToUpdate.getVat()* bookingKoiToUpdate.getTotalAmount());
                bookingKoiToUpdate.setUpdatedBy(authenticationService.getCurrentAccount());
                float totalBookingKoiAmount =0;
                List<BookingKoiDetail> bookingKoiDetails = iBookingKoiDetailRepository.showDetailOfBookingID(id);
                for(BookingKoiDetail b: bookingKoiDetails){
                    totalBookingKoiAmount +=b.getTotalAmount();
                }

                bookingKoiToUpdate.setTotalAmount(totalBookingKoiAmount);
                bookingKoiToUpdate.setTotalAmountWithVAT(bookingKoiToUpdate.getTotalAmount()+bookingKoiToUpdate.getVatAmount()-bookingKoiToUpdate.getDiscountAmount());
                return bookingRepository.save(bookingKoiToUpdate);
            }else{
                throw new NotUpdateException("Update booing id"+id+"failed");
            }
       }catch (NotUpdateException e){
           throw new GenericException(e.getMessage());
       }
    }
    @Override
    public List<Bookings> getKoiBooking(Long accountID) {
        return bookingRepository.listKoiBooking(accountID);
    }

}
