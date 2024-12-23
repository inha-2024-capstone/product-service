package com.yoger.productserviceorganization.priceOffer.application;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.yoger.productserviceorganization.priceOffer.adapters.web.dto.request.ConfirmOfferRequestDTO;
import com.yoger.productserviceorganization.priceOffer.adapters.web.dto.request.PriceOfferRequestDTO;
import com.yoger.productserviceorganization.priceOffer.domain.exception.PriceOfferAlreadyExistException;
import com.yoger.productserviceorganization.priceOffer.domain.exception.PriceOfferNotAllowedCreateOrUpdateException;
import com.yoger.productserviceorganization.priceOffer.domain.model.PriceOffer;
import com.yoger.productserviceorganization.priceOffer.domain.model.PriceOfferState;
import com.yoger.productserviceorganization.priceOffer.application.port.PriceOfferRepository;
import com.yoger.productserviceorganization.priceOffer.mapper.PriceOfferMapper;
import com.yoger.productserviceorganization.product.application.ProductService;
import com.yoger.productserviceorganization.product.domain.model.PriceByQuantity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PriceOfferServiceImplTest {

    @InjectMocks
    private PriceOfferServiceImpl priceOfferService;

    @Mock
    private PriceOfferRepository priceOfferRepository;

    @Mock
    private ProductService productService;

    @Captor
    private ArgumentCaptor<PriceOffer> priceOfferCaptor = ArgumentCaptor.forClass(PriceOffer.class);


    Long userId = 1L;
    Long productId = 1L;
    Long companyId = 2L;
    List<PriceByQuantity> priceByQuantities = List.of(new PriceByQuantity(1, 1000));
    PriceOfferRequestDTO priceOfferRequestDTO = new PriceOfferRequestDTO(priceByQuantities);


    @Test
    void create_success() {
        given(priceOfferRepository.findById(productId, companyId)).willReturn(Optional.empty());

        priceOfferService.create(productId, companyId, priceOfferRequestDTO);

        verify(priceOfferRepository, times(1)).save(priceOfferCaptor.capture());
        PriceOffer captured = priceOfferCaptor.getValue();
        PriceOffer expected = PriceOfferMapper.createTemporary(productId, companyId, priceByQuantities);
        assertThat(captured).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void create_failure() {
        PriceOffer priceOffer = PriceOffer.of(productId, companyId, priceByQuantities, PriceOfferState.TEMPORARY);
        Optional<PriceOffer> optionalPriceOffer = Optional.of(priceOffer);
        given(priceOfferRepository.findById(productId, companyId)).willReturn(optionalPriceOffer);

        assertThatThrownBy(() -> priceOfferService.create(productId, companyId, priceOfferRequestDTO))
                .isInstanceOf(PriceOfferAlreadyExistException.class);
    }

    @Test
    void update_success() {
        given(productService.isDemoProduct(productId)).willReturn(true);

        priceOfferService.update(productId, companyId, priceOfferRequestDTO);

        verify(priceOfferRepository, times(1)).save(priceOfferCaptor.capture());
        PriceOffer captured = priceOfferCaptor.getValue();
        PriceOffer expected = PriceOfferMapper.createTemporary(productId, companyId, priceByQuantities);
        assertThat(captured).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void update_fail() {
        given(productService.isDemoProduct(productId)).willReturn(false);

        assertThatThrownBy(() -> priceOfferService.update(productId, companyId, priceOfferRequestDTO))
                .isInstanceOf(PriceOfferNotAllowedCreateOrUpdateException.class);
    }

    @Test
    void confirm() {
        ConfirmOfferRequestDTO confirmOfferRequestDTO = new ConfirmOfferRequestDTO(companyId, LocalDateTime.MAX);
        PriceOffer priceOffer = PriceOffer.createTemporary(productId, companyId, priceByQuantities);
        given(priceOfferRepository.findById(productId, companyId)).willReturn(Optional.of(priceOffer));
        priceOfferService.confirm(productId, userId, confirmOfferRequestDTO);

        verify(priceOfferRepository, times(1)).save(priceOfferCaptor.capture());
        PriceOffer captured = priceOfferCaptor.getValue();
        PriceOffer expected = PriceOffer.of(productId, companyId, priceByQuantities, PriceOfferState.CONFIRMED);
        assertThat(captured).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void delete_success() {
        given(productService.isDemoProduct(productId)).willReturn(true);

        priceOfferService.delete(productId, companyId);

        verify(priceOfferRepository, times(1)).delete(productId, companyId);
    }

    @Test
    void delete_failure() {
        given(productService.isDemoProduct(productId)).willReturn(false);

        assertThatThrownBy(() -> priceOfferService.delete(productId, companyId))
                .isInstanceOf(PriceOfferNotAllowedCreateOrUpdateException.class);
    }
}