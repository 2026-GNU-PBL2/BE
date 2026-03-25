
package pbl2.sub119.backend.subproduct.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pbl2.sub119.backend.subproduct.entity.SubProduct;

import java.util.List;
import java.util.Optional;

@Mapper
public interface SubProductMapper {

    List<SubProduct> findAll();

    Optional<SubProduct> findById(@Param("id") String id);

    boolean existsByServiceName(@Param("serviceName") String serviceName);

    boolean existsByServiceNameExcludeId(@Param("serviceName") String serviceName,
                                         @Param("id") String id);

    int save(SubProduct subProduct);

    int update(SubProduct subProduct);
}