package com.yoger.productserviceorganization.priceOffer.adapters.persistence;

import com.yoger.productserviceorganization.priceOffer.adapters.persistence.jpa.JpaPriceOfferRepository;
import com.yoger.productserviceorganization.priceOffer.adapters.persistence.jpa.PriceOfferId;
import com.yoger.productserviceorganization.priceOffer.domain.model.PriceOffer;
import com.yoger.productserviceorganization.priceOffer.application.port.PriceOfferRepository;
import com.yoger.productserviceorganization.priceOffer.mapper.PriceOfferMapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PriceOfferRepositoryImpl implements PriceOfferRepository {
    private final JpaPriceOfferRepository jpaPriceOfferRepository;

    @Override
    public void save(PriceOffer priceOffer) {
        jpaPriceOfferRepository.save(PriceOfferMapper.toEntityFrom(priceOffer));
    }

    @Override
    public Optional<PriceOffer> findById(Long productId, Long companyId) {
        return jpaPriceOfferRepository.findById(new PriceOfferId(productId, companyId))
                .map(PriceOfferMapper::toDomainFrom);
    }

    @Override
    public List<PriceOffer> findAllByProductId(Long productId) {
        return jpaPriceOfferRepository.findAllById_ProductId(productId)
                .stream().map(PriceOfferMapper::toDomainFrom)
                .toList();
    }

    @Override
    public void delete(Long productId, Long companyId) {
        jpaPriceOfferRepository.deleteById(new PriceOfferId(productId, companyId));
    }
}
