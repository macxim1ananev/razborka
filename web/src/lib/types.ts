export interface PageDto<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface ListingPreview {
  id: number;
  title: string;
  price: number;
  firstPhotoUrl: string;
  sellerCity: string;
  vehicleYear?: number;
  mileageKm?: number;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  refreshExpiresAt: string;
  tokenType: string;
}

export interface UserProfile {
  id: number;
  telegramId: number;
  username: string;
  firstName: string;
  phone: string;
  avatarUrl: string;
  city: string;
  bio: string;
  status: string;
  createdAt: string;
  lastOnline?: string;
}

export interface UserCar {
  id: number;
  displayName: string;
  brand: string;
  model: string;
  generation?: string;
  year?: number;
  engineVolume?: number;
  active: boolean;
  createdAt: string;
  updatedAt?: string;
}

export interface CategoryTreeNode {
  id: number;
  name: string;
  slug: string;
  level: number;
  children: CategoryTreeNode[];
  attributes: CategoryAttribute[];
}

export interface CategoryAttribute {
  id: number;
  name: string;
  slug: string;
  dataType: string;
  required: boolean;
  sortOrder: number;
}

export interface ListingDetail {
  id: number;
  title: string;
  description: string;
  price: number;
  condition: string;
  originalReplica: string;
  vin: string;
  catalogBlock?: string;
  status: string;
  vehicleYear?: number;
  mileageKm?: number;
  categoryId: number;
  categoryName: string;
  photoUrls: string[];
  compatibility: CompatibilityRow[];
  seller: SellerContact;
  moreFromSeller: ListingPreview[];
  createdAt: string;
  updatedAt?: string;
}

export interface CompatibilityRow {
  id: number;
  brand: string;
  model: string;
  generation: string;
  yearFrom?: number;
  yearTo?: number;
  engineVolume?: number;
}

export interface SellerContact {
  id: number;
  telegramUsername: string;
  firstName: string;
  city: string;
}

export interface SearchResponse {
  content: SearchHit[];
  totalElements: number;
  page: number;
  size: number;
  facets: Record<string, FacetBucket[]>;
}

export interface SearchHit {
  id: number;
  title: string;
  price: number;
  firstPhotoUrl: string;
  sellerCity: string;
  vehicleYear?: number;
  mileageKm?: number;
  partCondition: string;
  originalReplica: string;
  categoryId: number;
}

export interface FacetBucket {
  value: string;
  count: number;
}

export interface VehicleGenerationOption {
  id: number;
  name: string;
  yearFrom?: number;
  yearTo?: number;
}

export interface VehicleModelOption {
  id: number;
  name: string;
  generations: VehicleGenerationOption[];
}

export interface VehicleMakeOption {
  id: number;
  name: string;
  models: VehicleModelOption[];
}

export interface TelegramWidgetUser {
  id: number;
  first_name?: string;
  last_name?: string;
  username?: string;
  photo_url?: string;
  auth_date: number;
  hash: string;
}
