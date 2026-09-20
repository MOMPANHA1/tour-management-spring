package co.panha.hibernate.tourmanagement.features.tour;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * រូបភាពរបស់ Tour។
 *
 * <p><b>មិនពង្រីក {@code BaseEntity}</b> ដោយចេតនា — រូបភាពជាផ្នែករងរបស់ Tour មិនមែនធនធានឯករាជ្យ
 * ដែលមាន URL ផ្ទាល់ខ្លួនទេ ដូច្នេះមិនត្រូវការ {@code uuid} ឬ soft delete។ លុប Tour → រូបលុបតាម។
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "tour_images")
public class TourImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @Column(nullable = false, length = 255)
    private String url;

    @Column(length = 180)
    private String caption;

    private Integer sortOrder;
}
