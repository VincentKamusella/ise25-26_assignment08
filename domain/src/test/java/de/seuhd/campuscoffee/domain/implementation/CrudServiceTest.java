package de.seuhd.campuscoffee.domain.implementation;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import de.seuhd.campuscoffee.domain.model.objects.DomainModel;
import de.seuhd.campuscoffee.domain.ports.data.CrudDataService;

/**
 * Unit tests for the generic CrudServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
public class CrudServiceTest {

    /**
     * Simple domain model for testing.
     */
    static class TestDomain implements DomainModel<Long> {
        private final Long id;

        TestDomain(Long id) {
            this.id = id;
        }

        @Override
        public Long getId() {
            return id;
        }
    }

    /**
     * Concrete CrudServiceImpl implementation for TestDomain.
     */
    static class TestCrudService extends CrudServiceImpl<TestDomain, Long> {

        private final CrudDataService<TestDomain, Long> dataService;

        TestCrudService(CrudDataService<TestDomain, Long> dataService) {
            super(TestDomain.class);
            this.dataService = dataService;
        }

        @Override
        protected CrudDataService<TestDomain, Long> dataService() {
            return dataService;
        }
    }

    @Mock
    private CrudDataService<TestDomain, Long> crudDataService;

    private TestCrudService crudService;

    @BeforeEach
    void beforeEach() {
        crudService = new TestCrudService(crudDataService);
    }


    @Test
    void clearDelegatesToDataService() {
        crudService.clear();
        verify(crudDataService).clear();
    }

    @Test
    void getAllDelegatesToDataService() {
        List<TestDomain> list = List.of(new TestDomain(1L), new TestDomain(2L));
        when(crudDataService.getAll()).thenReturn(list);

        List<TestDomain> result = crudService.getAll();

        verify(crudDataService).getAll();
        assertThat(result).isEqualTo(list);
    }

    @Test
    void getByIdDelegatesToDataService() {
        TestDomain domain = new TestDomain(42L);
        when(crudDataService.getById(42L)).thenReturn(domain);

        TestDomain result = crudService.getById(42L);

        verify(crudDataService).getById(42L);
        assertThat(result).isSameAs(domain);
    }

    @Test
    void upsertCreatesWhenIdIsNull() {
        TestDomain toCreate = new TestDomain(null);
        TestDomain persisted = new TestDomain(10L);
        when(crudDataService.upsert(toCreate)).thenReturn(persisted);

        TestDomain result = crudService.upsert(toCreate);

        verify(crudDataService, never()).getById(anyLong()); // create path
        verify(crudDataService).upsert(toCreate);
        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    void upsertUpdatesWhenIdIsNotNull() {
        TestDomain toUpdate = new TestDomain(5L);
        TestDomain persisted = new TestDomain(5L);

        when(crudDataService.getById(5L)).thenReturn(persisted);
        when(crudDataService.upsert(toUpdate)).thenReturn(persisted);

        TestDomain result = crudService.upsert(toUpdate);

        verify(crudDataService).getById(5L);     // existence check
        verify(crudDataService).upsert(toUpdate);
        assertThat(result.getId()).isEqualTo(5L);
    }

    @Test
    void deleteDelegatesToDataService() {
        Long id = 7L;

        crudService.delete(id);

        verify(crudDataService).delete(id);
    }
}

