package com.smartparking.parking.service;

import com.smartparking.common.exception.BusinessException;
import com.smartparking.parking.dto.CreateSpaceRequest;
import com.smartparking.parking.dto.SpaceVO;
import com.smartparking.parking.entity.ParkingSpace;
import com.smartparking.parking.mapper.ParkingSpaceMapper;
import com.smartparking.parking.service.impl.ParkingSpaceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ParkingSpaceService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ParkingSpaceServiceTest {
    
    @Mock
    private ParkingSpaceMapper spaceMapper;
    
    @InjectMocks
    private ParkingSpaceServiceImpl spaceService;
    
    private CreateSpaceRequest request;
    private Long zoneId = 1L;
    
    @BeforeEach
    void setUp() {
        request = new CreateSpaceRequest();
        request.setSpaceNo("TEST_001");
        request.setSpaceType(1);
        request.setWidthCm(250);
        request.setLengthCm(500);
    }
    
    @Test
    void testCreateSpace_Success() {
        // Arrange
        when(spaceMapper.selectCount(any())).thenReturn(0L);
        when(spaceMapper.insert(any(ParkingSpace.class))).thenReturn(1);
        
        // Act
        SpaceVO result = spaceService.createSpace(zoneId, request);
        
        // Assert
        assertNotNull(result);
        assertEquals("TEST_001", result.getSpaceNo());
        assertEquals(1, result.getSpaceType());
        verify(spaceMapper, times(1)).insert(any(ParkingSpace.class));
    }
    
    @Test
    void testCreateSpace_DuplicateSpaceNo() {
        // Arrange
        when(spaceMapper.selectCount(any())).thenReturn(1L);
        
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            spaceService.createSpace(zoneId, request);
        });
        assertEquals("SPACE_002", exception.getMessage());
    }
    
    @Test
    void testOccupySpace_Success() {
        // Arrange
        ParkingSpace space = ParkingSpace.builder()
            .id(1L)
            .status(1)  // 空闲
            .build();
        
        when(spaceMapper.selectById(1L)).thenReturn(space);
        when(spaceMapper.updateById(any(ParkingSpace.class))).thenReturn(1);
        
        // Act
        spaceService.occupySpace(1L, "京 A12345");
        
        // Assert
        verify(spaceMapper, times(1)).updateById(any(ParkingSpace.class));
    }
    
    @Test
    void testOccupySpace_NotFound() {
        // Arrange
        when(spaceMapper.selectById(1L)).thenReturn(null);
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            spaceService.occupySpace(1L, "京 A12345");
        });
    }
    
    @Test
    void testReleaseSpace_Success() {
        // Arrange
        ParkingSpace space = ParkingSpace.builder()
            .id(1L)
            .status(0)  // 占用
            .build();
        
        when(spaceMapper.selectById(1L)).thenReturn(space);
        when(spaceMapper.updateById(any(ParkingSpace.class))).thenReturn(1);
        
        // Act
        spaceService.releaseSpace(1L);
        
        // Assert
        verify(spaceMapper, times(1)).updateById(any(ParkingSpace.class));
    }
}
