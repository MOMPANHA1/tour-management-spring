package co.panha.hibernate.tourmanagement.features.guide;

/**
 * ស្ថានភាពមគ្គុទ្ទេសក៍។
 *
 * <ul>
 *   <li>{@code ACTIVE} — ទទួលការចាត់តាំងបាន</li>
 *   <li>{@code ON_LEAVE} — សម្រាកបណ្តោះអាសន្ន</li>
 *   <li>{@code INACTIVE} — ឈប់ធ្វើការ (ដាក់មិនបានបើនៅមានកាលវិភាគអនាគត)</li>
 * </ul>
 */
public enum GuideStatus {
    ACTIVE,
    ON_LEAVE,
    INACTIVE
}
